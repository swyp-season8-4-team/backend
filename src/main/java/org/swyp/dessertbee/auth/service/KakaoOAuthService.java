package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.swyp.dessertbee.auth.oauth2.KakaoResponse;
import org.swyp.dessertbee.auth.oauth2.OAuth2Response;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
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
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;
import org.swyp.dessertbee.auth.exception.OAuthExceptions.*;
import org.swyp.dessertbee.auth.service.OAuthAccountLinkingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 카카오 OAuth 관련 처리를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final JWTUtil jwtUtil;
    private final ImageService imageService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PreferenceService preferenceService;
    private final OAuthAccountLinkingService oAuthAccountLinkingService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드로 카카오 로그인 처리
     */
    @Transactional
    public LoginResponse processKakaoLogin(String code, String deviceId, boolean isApp) {
        try {
            log.info("카카오 로그인 처리 시작 - 인가 코드: {}", code);

            // 1. 인가 코드로 액세스 토큰 요청
            String accessToken = getKakaoAccessToken(code);
            log.info("카카오 액세스 토큰 획득 성공");

            // 2. 액세스 토큰으로 사용자 정보 요청
            OAuth2Response userInfo = getKakaoUserInfo(accessToken);
            log.info("카카오 사용자 정보 획득 성공 - 이메일: {}", userInfo.getEmail());
            // 3. 사용자 정보로 회원가입/로그인 처리
            return processUserLogin(userInfo, deviceId, isApp);

        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            throw new OAuthServiceException("카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 인가 코드로 카카오 액세스 토큰 요청
     */
    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("client_secret", clientSecret);

        // HTTP 요청 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(body, headers);

        // 카카오 API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // 응답에서 액세스 토큰 추출
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new OAuthAuthenticationException("카카오 액세스 토큰을 획득하는데 실패했습니다.");
        }

        return (String) responseBody.get("access_token");
    }

    /**
     * 액세스 토큰으로 카카오 사용자 정보 요청
     */
    private OAuth2Response getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 엔티티 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // 카카오 API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        // 응답에서 사용자 정보 추출
        Map<String, Object> userAttributes = response.getBody();
        if (userAttributes == null) {
            throw new OAuthAuthenticationException("카카오 사용자 정보를 획득하는데 실패했습니다.");
        }

        return new KakaoResponse(userAttributes);
    }

    /**
     * OAuth 사용자 정보로 로그인 처리 (회원가입 또는 자동 계정 연결)
     */
    private LoginResponse processUserLogin(OAuth2Response oauth2Response, String deviceId, boolean isApp) {
        log.info("카카오 OAuth 로그인 처리 시작 - 이메일: {}, 제공자: {}", oauth2Response.getEmail(), oauth2Response.getProvider());
        
        // OAuth 자동 계정 통합 서비스를 사용하여 사용자 조회 및 계정 연결
        UserEntity user = oAuthAccountLinkingService.processOAuthUserLogin(oauth2Response, deviceId, isApp);
        
        // 만약 새로운 사용자인 경우 회원가입 처리
        if (user.getId() == null) {
            log.info("새로운 카카오 사용자 회원가입 - 이메일: {}", oauth2Response.getEmail());
            user = registerNewUser(oauth2Response);
        } else {
            log.info("기존 사용자 카카오 로그인 성공 (자동 계정 연결 포함) - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
        }

        // 정지 여부 확인
        if (user.isSuspended()) {
            throw new AccountLockedException("계정이 정지되었습니다. 정지 해제 일시: " + user.getSuspendedUntil());
        }

        // 사용자 역할 조회
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().getRoleName())
                .collect(Collectors.toList());

        // 토큰 발급
        boolean keepLoggedIn = isApp; // 기본값
        String accessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getUserUuid(), keepLoggedIn);
        long expiresIn = jwtUtil.getACCESS_TOKEN_EXPIRE();
        long refreshExpiresIn = keepLoggedIn ? jwtUtil.getLONG_REFRESH_TOKEN_EXPIRE() : jwtUtil.getSHORT_REFRESH_TOKEN_EXPIRE();

        // 리프레시 토큰 저장 (디바이스 ID 관리 포함)
        String usedDeviceId = tokenService.saveRefreshToken(
                user.getUserUuid(),
                refreshToken,
                oauth2Response.getProvider(),
                oauth2Response.getProviderId(),
                deviceId,
                keepLoggedIn  // 추가된 파라미터
        );

        // 프로필 이미지 조회
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
     */
    private UserEntity registerNewUser(OAuth2Response oauth2Response) {
        log.info("새 사용자 등록 - 이메일: {}", oauth2Response.getEmail());

        String uniqueNickname = generateUniqueNickname(oauth2Response.getNickname());
        // 사용자 엔티티 생성
        UserEntity user = UserEntity.builder()
                .email(oauth2Response.getEmail())
                .nickname(uniqueNickname)
                .build();

        // 기본 역할 설정 (USER)
        RoleEntity role = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "기본 사용자 역할을 찾을 수 없습니다."));
        user.addRole(role);
        UserEntity savedUser = userRepository.save(user);

        // 카카오 프로필 이미지가 있는 경우 다운로드하여 S3에 저장
        String profileImageUrl = oauth2Response.getImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // S3 폴더 경로 설정
            String folder = String.format("profile/%d", savedUser.getId());

            // 이미지 서비스를 통해 외부 URL 이미지 다운로드 및 저장
            imageService.downloadAndSaveImage(
                    profileImageUrl,
                    ImageType.PROFILE,
                    savedUser.getId(),
                    folder
            );
        }
        return savedUser;
    }

    private String generateUniqueNickname(String baseNickname) {
        // 기본 닉네임이 이미 사용 가능한 경우 그대로 반환
        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }

        // 최대 100번 시도하여 고유한 닉네임 생성
        for (int i = 1; i <= 100; i++) {
            String candidateNickname = baseNickname + " " + generateRandomNumber();
            if (!userRepository.existsByNickname(candidateNickname)) {
                return candidateNickname;
            }
        }

        // 100번 시도 후에도 고유한 닉네임을 찾지 못한 경우 예외 발생
        throw new DuplicateNicknameException("고유한 닉네임을 생성할 수 없습니다.");
    }

    private String generateRandomNumber() {
        // 4자리 난수 생성
        return String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 카카오 액세스 토큰으로 직접 로그인 처리 (Flutter 앱용)
     */
    @Transactional
    public LoginResponse processKakaoTokenLogin(String accessToken, String deviceId, boolean isApp) {
        try {
            log.info("카카오 액세스 토큰 직접 로그인 처리 시작");

            // 1. 액세스 토큰으로 사용자 정보 요청
            OAuth2Response userInfo = getKakaoUserInfo(accessToken);
            log.info("카카오 사용자 정보 획득 성공 - 이메일: {}", userInfo.getEmail());

            // 2. 사용자 정보로 회원가입/로그인 처리
            return processUserLogin(userInfo, deviceId, isApp);

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 로그인 처리 중 오류 발생", e);
            throw new OAuthServiceException("카카오 액세스 토큰 로그인 처리 중 오류가 발생했습니다.");
        }
    }

}