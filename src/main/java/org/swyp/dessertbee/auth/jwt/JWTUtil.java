package org.swyp.dessertbee.auth.jwt;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.auth.enums.TokenType;
import org.swyp.dessertbee.auth.exception.AuthExceptions;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 */
/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 */
@Component
@Slf4j
public class JWTUtil {

    private final SecretKey accessTokenSecretKey;
    private final SecretKey refreshTokenSecretKey;

    private final long EMAIL_VERIFICATION_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L;

    @Getter
    private final long SHORT_ACCESS_TOKEN_EXPIRE = 24 * 60 * 60 * 1000L;       // 1일
    @Getter
    private final long LONG_ACCESS_TOKEN_EXPIRE = 3 * 24 * 60 * 60 * 1000L;   // 3일

    @Getter
    private final long SHORT_REFRESH_TOKEN_EXPIRE = 10 * 24 * 60 * 60 * 1000L;   // 10일
    @Getter
    private final long LONG_REFRESH_TOKEN_EXPIRE = 30 * 24 * 60 * 60 * 1000L;   // 30일

    // 개발용 토큰 만료 시간 설정 (밀리초)
    @Getter
    private final long DEV_ACCESS_TOKEN_EXPIRE = 3 * 60 * 1000L;  // 3분
    @Getter
    private final long DEV_REFRESH_TOKEN_EXPIRE = 5 * 60 * 1000L; // 5분

    public JWTUtil(
            @Value("${spring.jwt.secret.access}") String accessSecret,
            @Value("${spring.jwt.secret.refresh}") String refreshSecret
    ) {
        this.accessTokenSecretKey = new SecretKeySpec(
                accessSecret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.refreshTokenSecretKey = new SecretKeySpec(
                refreshSecret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(UUID userUuid, List<String> roles, boolean keepLoggedIn) {
        long expireTime = keepLoggedIn ? LONG_ACCESS_TOKEN_EXPIRE : SHORT_ACCESS_TOKEN_EXPIRE;
        return buildToken(TokenType.ACCESS, userUuid, roles, null, null, accessTokenSecretKey, expireTime);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(UUID userUuid, boolean keepLoggedIn) {
        long expireTime = keepLoggedIn ? LONG_REFRESH_TOKEN_EXPIRE : SHORT_REFRESH_TOKEN_EXPIRE;
        return buildToken(TokenType.REFRESH, userUuid, null, null,null, refreshTokenSecretKey, expireTime);
    }

    /**
     * 개발 환경용 짧은 만료 시간을 가진 액세스 토큰 생성 (3분)
     *
     * @param roles 사용자 권한 목록
     * @return 생성된 개발용 액세스 토큰
     */
    public String createDevAccessToken(UUID userUuid, List<String> roles) {
        log.debug("개발용 액세스 토큰 생성 - 만료 시간: {}분", DEV_ACCESS_TOKEN_EXPIRE / (60 * 1000));
        return buildToken(TokenType.ACCESS, userUuid, roles, null, null, accessTokenSecretKey, DEV_ACCESS_TOKEN_EXPIRE);
    }


    /**
     * 개발 환경용 짧은 만료 시간을 가진 리프레시 토큰 생성 (5분)
     *
     * @return 생성된 개발용 리프레시 토큰
     */
    public String createDevRefreshToken(UUID userUuid) {
        log.debug("개발용 리프레시 토큰 생성 - 만료 시간: {}분", DEV_REFRESH_TOKEN_EXPIRE / (60 * 1000));
        return buildToken(TokenType.REFRESH, userUuid, null, null, null, refreshTokenSecretKey, DEV_REFRESH_TOKEN_EXPIRE);
    }

    /**
     * 이메일 인증 토큰 생성
     */
    public String createEmailVerificationToken(Long verificationId, EmailVerificationPurpose purpose) {
        return buildToken(TokenType.EMAIL_VERIFICATION, null, null, verificationId, purpose, accessTokenSecretKey, EMAIL_VERIFICATION_TOKEN_EXPIRE_TIME);
    }

    /**
     * JWT 토큰 생성 공통 메서드
     */
    private String buildToken(
            TokenType tokenType,
            UUID userUuid,
            List<String> roles,
            Long verificationId,
            EmailVerificationPurpose purpose,
            SecretKey secretKey,
            long expireTime
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime expirationTime = now.plus(Duration.ofMillis(expireTime));

        JwtBuilder builder = Jwts.builder()
                // 공통 표준 클레임
                .issuer("dessertbee.com")                          // iss: 토큰 발급자
                .issuedAt(Date.from(now.toInstant()))              // iat: 발급 시간
                .expiration(Date.from(expirationTime.toInstant())) // exp: 만료 시간
                .id(UUID.randomUUID().toString())                  // jti: 토큰 고유 식별자
                .claim("type", tokenType.name());                  // 토큰 유형

        // 토큰 유형별 특정 클레임 추가
        switch (tokenType) {
            case ACCESS:
                builder.subject(userUuid.toString());              // sub: 사용자 UUID
                builder.claim("roles", roles);                     // 역할 정보
                break;

            case REFRESH:
                builder.subject(userUuid.toString());              // sub: 사용자 UUID (식별용)
                break;

            case EMAIL_VERIFICATION:
                builder.subject("EMAIL_VERIFICATION_" + purpose.name()); // sub: 이메일 인증 목적
                if (verificationId != null) {
                    builder.claim("verificationId", verificationId); // 인증 ID 추가
                }
                break;
        }

        return builder.signWith(secretKey).compact();
    }


    /**
     * 토큰 유효성 검증
     */
    /**
     * JWT 토큰 유효성 검증
     * @param token 검증할 토큰
     * @param isAccessToken 액세스 토큰 여부 (true: 액세스 토큰, false: 리프레시 토큰)
     * @return 검증 결과 에러코드 (null이면 유효한 토큰)
     */
    public ErrorCode validateToken(String token, boolean isAccessToken) {
        if (token == null) {
            return ErrorCode.JWT_TOKEN_MISSING;
        }

        SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return null; // 유효한 토큰
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            return ErrorCode.JWT_TOKEN_EXPIRED;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
            return ErrorCode.JWT_SIGNATURE_INVALID;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            return ErrorCode.JWT_TOKEN_UNSUPPORTED;
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
            return ErrorCode.JWT_TOKEN_MALFORMED;
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예상치 못한 오류: {}", e.getMessage());
            return ErrorCode.AUTHENTICATION_FAILED;
        }
    }

    /**
     * 토큰에서 권한 정보 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token, boolean isAccessToken) {
        SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
        return (List<String>) parseClaims(token, key)
                .get("roles", List.class);
    }

    /**
     * 토큰에서 uuid 정보 추출
     */
    public UUID getUserUuid(String token, boolean isAccessToken) {
        SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
        Claims claims = parseClaims(token, key);

        // 토큰 유형 확인
        String tokenType = claims.get("type", String.class);
        if (tokenType == null ||
                !(tokenType.equals(TokenType.ACCESS.name()) || tokenType.equals(TokenType.REFRESH.name()))) {
            throw new AuthExceptions.InvalidVerificationTokenException("유효한 사용자 토큰이 아닙니다");
        }

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Claims 파싱
     */
    private Claims parseClaims(String token, SecretKey secretKey) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


    /**
     * 이메일 인증 토큰에서 목적 추출
     */
    public EmailVerificationPurpose getVerificationPurpose(String token) {
        Claims claims = parseClaims(token, accessTokenSecretKey);

        // 이메일 인증 토큰인지 확인
        String tokenType = claims.get("type", String.class);
        if (tokenType == null || !tokenType.equals(TokenType.EMAIL_VERIFICATION.name())) {
            throw new AuthExceptions.InvalidVerificationTokenException("유효한 이메일 인증 토큰이 아닙니다");
        }

        String subject = claims.getSubject();
        if (subject != null && subject.startsWith("EMAIL_VERIFICATION_")) {
            String purposeName = subject.substring("EMAIL_VERIFICATION_".length());
            try {
                return EmailVerificationPurpose.valueOf(purposeName);
            } catch (Exception e) {
                throw new AuthExceptions.InvalidVerificationTokenException("이메일 인증 목적을 찾을 수 없습니다");
            }
        }

        throw new AuthExceptions.InvalidVerificationTokenException("이메일 인증 목적을 찾을 수 없습니다");
    }

    /**
     * 이메일 인증 토큰에서 인증 ID 추출
     */
    public Long getVerificationId(String token) {
        Claims claims = parseClaims(token, accessTokenSecretKey);

        // 이메일 인증 토큰인지 확인
        String tokenType = claims.get("type", String.class);
        if (tokenType == null || !tokenType.equals(TokenType.EMAIL_VERIFICATION.name())) {
            throw new AuthExceptions.InvalidVerificationTokenException("유효한 이메일 인증 토큰이 아닙니다");
        }

        return claims.get("verificationId", Long.class);
    }
}