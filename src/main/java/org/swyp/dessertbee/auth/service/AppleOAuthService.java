package org.swyp.dessertbee.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.swyp.dessertbee.auth.dto.request.AppleLoginRequest.AppleUserInfo;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.exception.AuthExceptions.DuplicateNicknameException;
import org.swyp.dessertbee.auth.exception.OAuthExceptions.OAuthAuthenticationException;
import org.swyp.dessertbee.auth.exception.AuthExceptions.OAuthServiceException;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.oauth2.AppleResponse;
import org.swyp.dessertbee.auth.oauth2.OAuth2Response;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.preference.service.PreferenceService;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.auth.service.OAuthAccountLinkingService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.interfaces.ECPrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Apple OAuth 인증 서비스
 * 웹과 앱 환경에서 Apple ID 로그인을 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final JWTUtil jwtUtil;
    private final ImageService imageService;
    private final PreferenceService preferenceService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuthAccountLinkingService oAuthAccountLinkingService;

    @Value("${APPLE_TEAM_ID}")
    private String teamId;

    @Value("${APPLE_KEY_ID}")
    private String keyId;

    @Value("${APPLE_KEY_PATH}")
    private String privateKeyPath;

    @Value("${spring.security.oauth2.client.provider.apple.token-uri}")
    private String tokenUri;

    // 웹용 Client ID
    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String webClientId;

    // 앱용 Client ID
    @Value("${spring.security.oauth2.client.registration.apple-app.client-id:${spring.security.oauth2.client.registration.apple.client-id}}")
    private String appClientId;

    /**
     * Apple 로그인 처리 - 기존 웹 로그인 또는 확장된 앱 로그인을 구분하여 처리
     *
     * @param code Apple에서 제공한 인가 코드
     * @param idToken Apple에서 제공한 ID 토큰
     * @param state CSRF 방지를 위한 상태값
     * @param userInfo 최초 로그인 시 Apple에서 제공하는 사용자 정보 (선택적)
     * @param deviceId 디바이스 식별자
     * @param isApp 앱에서의 로그인 여부
     * @return 로그인 응답 객체
     */
    @Transactional
    public LoginResponse processAppleLogin(String code, String idToken, String state, AppleUserInfo userInfo,
                                           String deviceId, boolean isApp) {
        try {
            log.info("애플 로그인 처리 시작 - 앱: {}, ID 토큰 존재 여부: {}, 코드 존재 여부: {}",
                    isApp, idToken != null, code != null);

            // 앱 로그인과 웹 로그인을 분리하여 처리
            if (isApp) {
                return processAppAppleLogin(idToken, userInfo, deviceId);
            } else {
                return processWebAppleLogin(code, idToken, state, userInfo, deviceId);
            }
        } catch (Exception e) {
            log.error("애플 로그인 처리 중 오류 발생 - 앱: {}", isApp, e);
            throw new OAuthServiceException("애플 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 웹 환경에서 Apple 로그인 처리
     * 인증 코드로 Apple 서버에 토큰 요청 후 사용자 인증
     *
     * @param code Apple에서 제공한 인가 코드
     * @param idToken Apple에서 제공한 ID 토큰 (선택적)
     * @param state CSRF 방지를 위한 상태값
     * @param userInfo 최초 로그인 시 Apple에서 제공하는 사용자 정보 (선택적)
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 객체
     */
    @Transactional
    protected LoginResponse processWebAppleLogin(String code, String idToken, String state, AppleUserInfo userInfo, String deviceId) {
        log.info("웹 애플 로그인 처리 시작 - 코드: {}, ID 토큰 존재 여부: {}",
                code != null ? "제공됨" : "없음", idToken != null);

        // 코드와 ID 토큰 중 하나는 필수
        if ((code == null || code.isEmpty()) && (idToken == null || idToken.isEmpty())) {
            throw new OAuthAuthenticationException("인증 코드 또는 ID 토큰이 필요합니다.");
        }

        String appleIdToken = idToken;

        // 코드만 제공된 경우 ID 토큰 요청
        if ((idToken == null || idToken.isEmpty()) && StringUtils.hasText(code)) {
            String clientSecret = createClientSecret();
            appleIdToken = getAppleIdToken(code, clientSecret);
            log.info("애플 인증 코드로 ID 토큰 획득 성공");
        }

        // ID 토큰 검증
        if (!verifyAppleIdToken(appleIdToken, false)) { // 웹 환경 검증 (isApp = false)
            throw new OAuthAuthenticationException("유효하지 않은 애플 ID 토큰입니다.");
        }

        // ID 토큰에서 사용자 정보 추출 (추가 사용자 정보가 있으면 보강)
        OAuth2Response oAuth2Response = getAppleUserInfo(appleIdToken, userInfo);
        log.info("애플 사용자 정보 획득 성공 - 이메일: {}", oAuth2Response.getEmail());

        // 사용자 로그인 또는 회원가입 처리 (웹 로그인)
        return processUserLogin(oAuth2Response, deviceId, false);
    }

    /**
     * 앱 환경에서 Apple 로그인 처리
     * 앱에서 받은 ID 토큰을 검증하고 사용자 인증
     *
     * @param idToken Apple에서 제공한 ID 토큰 (필수)
     * @param userInfo 최초 로그인 시 Apple에서 제공하는 사용자 정보 (선택적)
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 객체
     */
    @Transactional
    protected LoginResponse processAppAppleLogin(String idToken, AppleUserInfo userInfo, String deviceId) {
        log.info("앱 애플 로그인 처리 시작 - ID 토큰 존재 여부: {}", idToken != null);

        // 앱 로그인에서는 ID 토큰 필수
        if (idToken == null || idToken.isEmpty()) {
            throw new OAuthAuthenticationException("앱 로그인 시 ID 토큰은 필수입니다.");
        }

        // ID 토큰 검증 (앱 토큰은 별도 검증 로직 적용)
        if (!verifyAppleIdToken(idToken, true)) {
            throw new OAuthAuthenticationException("유효하지 않은 애플 ID 토큰입니다.");
        }

        // ID 토큰에서 사용자 정보 추출 (추가 사용자 정보가 있으면 보강)
        OAuth2Response oAuth2Response = getAppleUserInfo(idToken, userInfo);
        log.info("앱 애플 사용자 정보 획득 성공 - 이메일: {}", oAuth2Response.getEmail());

        // 사용자 로그인 또는 회원가입 처리 (앱 로그인)
        return processUserLogin(oAuth2Response, deviceId, true);
    }

    /**
     * Apple Client Secret 생성
     * Apple의 인증 서버에 요청하기 위한 JWT 형식의 client secret을 생성
     *
     * @return 생성된 client secret
     */
    private String createClientSecret() {
        try {
            ECPrivateKey privateKey = getPrivateKey();

            Algorithm algorithm = Algorithm.ECDSA256(null, privateKey);

            long now = System.currentTimeMillis();
            String jwt = JWT.create()
                    .withIssuer(teamId)
                    .withIssuedAt(new Date(now))
                    .withExpiresAt(new Date(now + 86400 * 1000L)) // 24시간 유효
                    .withAudience("https://appleid.apple.com")
                    .withSubject(webClientId) // 웹용 클라이언트 ID 사용
                    .withKeyId(keyId)
                    .sign(algorithm);

            return jwt;
        } catch (Exception e) {
            throw new OAuthServiceException("Apple client secret 생성 실패: " + e.getMessage());
        }
    }


    /**
     * Apple 비공개 키 로드
     *
     * @return ECPrivateKey 형식의 비공개 키
     */
    private ECPrivateKey getPrivateKey() {
        try {
            File keyFile = new File(privateKeyPath);
            StringBuilder privateKeyBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("PRIVATE KEY")) continue;
                    privateKeyBuilder.append(line.trim());
                }
            }

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBuilder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new OAuthServiceException("Apple 개인 키 로딩 실패: " + e.getMessage());
        }
    }

    /**
     * Apple 토큰 엔드포인트에 요청하여 ID 토큰 획득
     *
     * @param code Apple에서 제공한 인가 코드
     * @param clientSecret 생성된 client secret
     * @return 획득한 ID 토큰
     */
    private String getAppleIdToken(String code, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", webClientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id_token")) {
            throw new OAuthAuthenticationException("애플 ID 토큰을 획득하는데 실패했습니다.");
        }

        return (String) responseBody.get("id_token");
    }

    /**
     * ID 토큰 검증
     * 앱/웹 환경에 따라 다른 Client ID로 검증
     *
     * @param idToken 검증할 ID 토큰
     * @param isApp 앱 환경 여부
     * @return 검증 결과 (true: 유효, false: 유효하지 않음)
     */
    private boolean verifyAppleIdToken(String idToken, boolean isApp) {
        try {
            // 토큰 형식 기본 검증
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                log.warn("ID 토큰 형식이 잘못되었습니다.");
                return false;
            }

            // 페이로드 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // 필수 필드 존재 확인
            if (!claims.containsKey("sub") || !claims.containsKey("iss") || !claims.containsKey("aud")) {
                log.warn("ID 토큰에 필수 필드가 누락되었습니다.");
                return false;
            }

            // 발행자 확인
            String issuer = (String) claims.get("iss");
            if (!"https://appleid.apple.com".equals(issuer)) {
                log.warn("ID 토큰의 발행자가 올바르지 않습니다: {}", issuer);
                return false;
            }

            // 만료 시간 확인 (현재 시간이 만료 시간보다 이전이어야 함)
            if (claims.containsKey("exp")) {
                long expTime = ((Number) claims.get("exp")).longValue();
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime > expTime) {
                    log.warn("ID 토큰이 만료되었습니다. 만료 시간: {}, 현재 시간: {}", expTime, currentTime);
                    return false;
                }
            }

            // 대상 확인 (앱/웹에 따라 다른 Client ID 사용)
            String audience = (String) claims.get("aud");
            String expectedClientId = isApp ? appClientId : webClientId;

            // 환경에 맞는 Client ID 확인
            if (expectedClientId.equals(audience)) {
                return true; // 일치하면 유효
            }

            // 호환성을 위한 추가 검증: 다른 환경의 Client ID와도 비교
            if (isApp && webClientId.equals(audience)) {
                log.info("앱 요청이지만 웹용 Client ID로 검증됨: {}", audience);
                return true;
            } else if (!isApp && appClientId.equals(audience)) {
                log.info("웹 요청이지만 앱용 Client ID로 검증됨: {}", audience);
                return true;
            }

            log.warn("ID 토큰의 대상이 올바르지 않습니다. 예상: {}, 실제: {}", expectedClientId, audience);
            return false;

        } catch (Exception e) {
            log.error("ID 토큰 검증 중 오류 발생", e);
            return false;
        }
    }

    /**
     * Apple ID 토큰에서 사용자 정보 추출 및 추가 사용자 정보로 보강
     *
     * @param idToken Apple에서 제공한 ID 토큰
     * @param userInfo 최초 로그인 시 추가로 제공되는 사용자 정보 (선택적)
     * @return 표준화된 OAuth2Response 객체
     */
    private OAuth2Response getAppleUserInfo(String idToken, AppleUserInfo userInfo) {
        try {
            String[] parts = idToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // 추가 사용자 정보가 있는 경우 claims에 추가
            if (userInfo != null) {
                if (userInfo.getEmail() != null) {
                    claims.putIfAbsent("email", userInfo.getEmail());
                }

                if (userInfo.getName() != null) {
                    claims.putIfAbsent("firstName", userInfo.getName().getFirstName());
                    claims.putIfAbsent("lastName", userInfo.getName().getLastName());
                }
            }

            return new AppleResponse(claims);
        } catch (Exception e) {
            throw new OAuthAuthenticationException("애플 ID 토큰 디코딩 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 로그인 또는 회원가입 처리 (자동 계정 연결 포함)
     *
     * @param oauth2Response 표준화된 OAuth2Response 객체
     * @param deviceId 디바이스 식별자
     * @param isApp 앱에서의 로그인 여부
     * @return 로그인 응답 객체
     */
    private LoginResponse processUserLogin(OAuth2Response oauth2Response, String deviceId, boolean isApp) {
        log.info("애플 OAuth 로그인 처리 시작 - 이메일: {}, 제공자: {}", oauth2Response.getEmail(), oauth2Response.getProvider());
        
        // 디바이스 ID가 없는 경우 생성
        String effectiveDeviceId = deviceId;
        if (effectiveDeviceId == null || effectiveDeviceId.isEmpty()) {
            effectiveDeviceId = tokenService.generateDeviceId();
            log.debug("새 디바이스 ID 생성: {}, 앱: {}", effectiveDeviceId, isApp);
        }

        // OAuth 자동 계정 통합 서비스를 사용하여 사용자 조회 및 계정 연결
        UserEntity user = oAuthAccountLinkingService.processOAuthUserLogin(oauth2Response, effectiveDeviceId, isApp);
        
        // 만약 새로운 사용자인 경우 회원가입 처리
        if (user.getId() == null) {
            log.info("새로운 애플 사용자 회원가입 - 이메일: {}", oauth2Response.getEmail());
            user = registerNewUser(oauth2Response, isApp);
        } else {
            log.info("기존 사용자 애플 로그인 성공 (자동 계정 연결 포함) - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
        }

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().getRoleName())
                .collect(Collectors.toList());

        // 앱에서 로그인한 경우 "keep logged in" 설정
        boolean keepLoggedIn = isApp; // 앱에서는 기본적으로 로그인 유지
        String accessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getUserUuid(), keepLoggedIn);
        long expiresIn = jwtUtil.getACCESS_TOKEN_EXPIRE();
        long refreshExpiresIn = keepLoggedIn ? jwtUtil.getLONG_REFRESH_TOKEN_EXPIRE() : jwtUtil.getSHORT_REFRESH_TOKEN_EXPIRE();

        // 토큰 저장
        String usedDeviceId = tokenService.saveRefreshToken(
                user.getUserUuid(),
                refreshToken,
                oauth2Response.getProvider(),
                oauth2Response.getProviderId(),
                deviceId,
                keepLoggedIn  // 추가된 파라미터
        );

        List<String> profileImages = imageService.getImagesByTypeAndId(
                ImageType.PROFILE, user.getId());
        String profileImageUrl = profileImages.isEmpty() ? null : profileImages.get(0);

        boolean isPreferenceSet = preferenceService.isUserPreferenceSet(user);

        // 계정 연결 정보 확인
        boolean accountLinkingOccurred = oAuthAccountLinkingService.isAccountLinkingOccurred();
        java.util.List<String> linkedProviders = oAuthAccountLinkingService.getUserOAuthProviders(user.getEmail());
        
        // 계정 연결 상태 초기화
        oAuthAccountLinkingService.resetAccountLinkingStatus();

        return LoginResponse.success(accessToken, refreshToken, expiresIn, refreshExpiresIn,
                user, profileImageUrl, usedDeviceId, isPreferenceSet, accountLinkingOccurred, linkedProviders);
    }

    /**
     * 새 사용자 등록
     *
     * @param oauth2Response 표준화된 OAuth2Response 객체
     * @param isApp 앱에서의 등록 여부
     * @return 생성된 사용자 엔티티
     */
    private UserEntity registerNewUser(OAuth2Response oauth2Response, boolean isApp) {
        log.info("새 사용자 등록 - 이메일: {}, 앱: {}", oauth2Response.getEmail(), isApp);

        String uniqueNickname = generateUniqueNickname(oauth2Response.getNickname());

        UserEntity user = UserEntity.builder()
                .email(oauth2Response.getEmail())
                .nickname(uniqueNickname)
                .build();

        // 앱에서 등록 여부 로그만 남기고 필드 설정 시도는 제거
        if (isApp) {
            log.info("앱에서 등록된 사용자: {}", oauth2Response.getEmail());
        }

        RoleEntity role = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "기본 사용자 역할을 찾을 수 없습니다."));
        user.addRole(role);

        return userRepository.save(user);
    }
    /**
     * 중복되지 않는 고유한 닉네임 생성
     *
     * @param baseNickname 기본 닉네임
     * @return 고유한 닉네임
     */
    private String generateUniqueNickname(String baseNickname) {
        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }

        for (int i = 1; i <= 100; i++) {
            String candidate = baseNickname + " " + generateRandomNumber();
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        throw new DuplicateNicknameException("고유한 닉네임을 생성할 수 없습니다.");
    }

    /**
     * 랜덤 숫자 생성 (닉네임 중복 방지용)
     *
     * @return 4자리 랜덤 숫자 문자열
     */
    private String generateRandomNumber() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
