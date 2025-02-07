package org.swyp.dessertbee.auth.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

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

    private final long SHORT_ACCESS_TOKEN_EXPIRE = 24 * 30 * 60 * 1000L;       // 1일
    private final long LONG_ACCESS_TOKEN_EXPIRE = 3 * 24 * 60 * 60 * 1000L;   // 3일

    private final long SHORT_REFRESH_TOKEN_EXPIRE = 10 * 24 * 60 * 60 * 1000L;   // 10일
    private final long LONG_REFRESH_TOKEN_EXPIRE = 30 * 24 * 60 * 60 * 1000L;   // 30일


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
    public String createAccessToken(String email, List<String> roles, boolean keepLoggedIn) {
        long expireTime = keepLoggedIn ? LONG_ACCESS_TOKEN_EXPIRE : SHORT_ACCESS_TOKEN_EXPIRE;
        return createToken(email, roles, accessTokenSecretKey, expireTime);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String email, List<String> roles, boolean keepLoggedIn) {
        long expireTime = keepLoggedIn ? LONG_REFRESH_TOKEN_EXPIRE : SHORT_REFRESH_TOKEN_EXPIRE;
        return createToken(email, roles, refreshTokenSecretKey, expireTime);
    }

    /**
     * JWT 토큰 생성 공통 메서드
     */
    private String createToken(String email, List<String> roles, SecretKey secretKey, long expireTime) {
        return Jwts.builder()
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token, boolean isAccessToken) {
        try {
            SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmail(String token, boolean isAccessToken) {
        SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
        return parseClaims(token, key)
                .get("email", String.class);
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
     * 토큰 만료시간 확인
     */
    public long getTokenTimeToLive(String token, boolean isAccessToken) {
        SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
        Date expiration = parseClaims(token, key).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 이메일 인증 토큰 생성
     * @param email 인증된 이메일
     * @param purpose 인증 목적
     */
    public String createEmailVerificationToken(String email, EmailVerificationPurpose purpose) {
        return Jwts.builder()
                .claim("email", email)
                .claim("purpose", purpose.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EMAIL_VERIFICATION_TOKEN_EXPIRE_TIME))
                .signWith(accessTokenSecretKey)
                .compact();
    }

    /**
     * 이메일 인증 토큰에서 목적 추출
     */
    public EmailVerificationPurpose getVerificationPurpose(String token) {
        Claims claims = parseClaims(token, accessTokenSecretKey);
        return EmailVerificationPurpose.valueOf(claims.get("purpose", String.class));
    }
}