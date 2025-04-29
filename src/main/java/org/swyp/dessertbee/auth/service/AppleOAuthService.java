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

    @Value("${APPLE_KEY_PATH}") // e.g. classpath:AuthKey_ABC123XYZ.p8
    private String privateKeyPath;

    @Value("${spring.security.oauth2.client.provider.apple.token-uri}")
    private String tokenUri;

    @Transactional
    public LoginResponse processAppleLogin(String code, String deviceId) {
        try {
            log.info("애플 로그인 처리 시작 - 인가 코드: {}", code);

            String clientSecret = createClientSecret();

            String idToken = getAppleIdToken(code, clientSecret);
            log.info("애플 ID 토큰 획득 성공");

            OAuth2Response userInfo = getAppleUserInfo(idToken);
            log.info("애플 사용자 정보 획득 성공 - 이메일: {}", userInfo.getEmail());

            return processUserLogin(userInfo, deviceId);

        } catch (Exception e) {
            log.error("애플 로그인 처리 중 오류 발생", e);
            throw new OAuthServiceException("애플 로그인 처리 중 오류가 발생했습니다.");
        }
    }

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
            throw new OAuthServiceException("Apple client secret 생성 실패");
        }
    }

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
            throw new OAuthServiceException("Apple 개인 키 로딩 실패");
        }
    }

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

    private OAuth2Response getAppleUserInfo(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            return new AppleResponse(claims);
        } catch (Exception e) {
            throw new OAuthAuthenticationException("애플 ID 토큰 디코딩 실패");
        }
    }

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

    private String generateRandomNumber() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
