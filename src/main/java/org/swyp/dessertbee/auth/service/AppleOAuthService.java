package org.swyp.dessertbee.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.interfaces.ECPrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String clientId;

    @Value("${APPLE_TEAM_ID}")
    private String teamId;

    @Value("${APPLE_KEY_ID}")
    private String keyId;

    @Value("${APPLE_KEY_PATH}")
    private String privateKeyPath;

    @Value("${spring.security.oauth2.client.provider.apple.token-uri}")
    private String tokenUri;

    /**
     * Apple 로그인 처리 - 코드, ID 토큰, 상태값, 사용자 정보, 디바이스 ID를 함께 처리
     *
     * @param code Apple에서 제공한 인가 코드
     * @param idToken Apple에서 제공한 ID 토큰
     * @param state CSRF 방지를 위한 상태값
     * @param userInfo 최초 로그인 시 Apple에서 제공하는 사용자 정보 (선택적)
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 객체
     */
    @Transactional
    public LoginResponse processAppleLogin(String code, String idToken, String state, AppleUserInfo userInfo, String deviceId) {
        try {
            log.info("애플 로그인 처리 시작 ID 토큰 존재 여부: {}", idToken != null);

            // Apple의 인증 서버에서 토큰을 얻기 위한 client secret 생성
            String clientSecret = createClientSecret();

            // 프론트에서 ID 토큰을 직접 전달받은 경우, 별도로 요청하지 않음
            String appleIdToken = (idToken != null && !idToken.isEmpty())
                    ? idToken
                    : getAppleIdToken(code, clientSecret);

            log.info("애플 ID 토큰 획득 성공");

            // ID 토큰에서 사용자 정보 추출 (추가 사용자 정보가 있으면 보강)
            OAuth2Response oAuth2Response = getAppleUserInfo(appleIdToken, userInfo);
            log.info("애플 사용자 정보 획득 성공 - 이메일: {}", oAuth2Response.getEmail());

            // 사용자 로그인 또는 회원가입 처리
            return processUserLogin(oAuth2Response, deviceId);

        } catch (Exception e) {
            log.error("애플 로그인 처리 중 오류 발생", e);
            throw new OAuthServiceException("애플 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
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
                    .withSubject(clientId)
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
            ClassPathResource resource = new ClassPathResource(privateKeyPath.replaceFirst("classpath:", ""));
            StringBuilder privateKeyBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("PRIVATE KEY")) continue;
                    privateKeyBuilder.append(line);
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
        body.add("client_id", clientId);
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
     * 사용자 로그인 또는 회원가입 처리
     *
     * @param oauth2Response 표준화된 OAuth2Response 객체
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 객체
     */
    private LoginResponse processUserLogin(OAuth2Response oauth2Response, String deviceId) {
        UserEntity user = userRepository.findByEmail(oauth2Response.getEmail())
                .orElseGet(() -> registerNewUser(oauth2Response));

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().getRoleName())
                .collect(Collectors.toList());

        boolean keepLoggedIn = false;
        String accessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getUserUuid(), keepLoggedIn);
        long expiresIn = jwtUtil.getACCESS_TOKEN_EXPIRE();

        String usedDeviceId = tokenService.saveRefreshToken(
                user.getUserUuid(), refreshToken,
                oauth2Response.getProvider(), oauth2Response.getProviderId(), deviceId
        );

        List<String> profileImages = imageService.getImagesByTypeAndId(
                ImageType.PROFILE, user.getId());
        String profileImageUrl = profileImages.isEmpty() ? null : profileImages.get(0);

        boolean isPreferenceSet = preferenceService.isUserPreferenceSet(user);

        return LoginResponse.success(accessToken, refreshToken, expiresIn, user, profileImageUrl, usedDeviceId, isPreferenceSet);
    }

    /**
     * 새 사용자 등록
     *
     * @param oauth2Response 표준화된 OAuth2Response 객체
     * @return 생성된 사용자 엔티티
     */
    private UserEntity registerNewUser(OAuth2Response oauth2Response) {
        log.info("새 사용자 등록 - 이메일: {}", oauth2Response.getEmail());

        String uniqueNickname = generateUniqueNickname(oauth2Response.getNickname());

        UserEntity user = UserEntity.builder()
                .email(oauth2Response.getEmail())
                .nickname(uniqueNickname)
                .build();

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