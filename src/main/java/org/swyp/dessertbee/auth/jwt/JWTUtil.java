package org.swyp.dessertbee.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {
    private final SecretKey accessTokenSecretKey;
    private final SecretKey refreshTokenSecretKey;

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

    public String createAccessToken(String email, List<String> roles) {
        return createToken(email, roles, accessTokenSecretKey, 30 * 60 * 1000L); // 30분
    }

    public String createRefreshToken(String email, List<String> roles) {
        return createToken(email, roles, refreshTokenSecretKey, 14 * 24 * 60 * 60 * 1000L); // 14일
    }

    private String createToken(String email, List<String> roles, SecretKey secretKey, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 검증 메소드 추가
    public boolean validateToken(String token, boolean isAccessToken) {
        try {
            SecretKey key = isAccessToken ? accessTokenSecretKey : refreshTokenSecretKey;
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JWT에서 이메일 추출
    public String getEmail(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // JWT에서 권한(roles) 추출
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // JWT 파싱 유틸리티 메서드
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenSecretKey) // 기본적으로 access token용 키 사용
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Refresh 토큰 파싱을 위한 별도 메서드
    private Claims parseRefreshTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(refreshTokenSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰 타입에 따라 Claims 파싱
    public Claims getClaims(String token, boolean isAccessToken) {
        return isAccessToken ? parseClaims(token) : parseRefreshTokenClaims(token);
    }

    // 토큰 타입에 따른 이메일 추출
    public String getEmail(String token, boolean isAccessToken) {
        try {
            Claims claims = getClaims(token, isAccessToken);
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // 토큰 타입에 따른 권한 추출
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token, boolean isAccessToken) {
        try {
            Claims claims = getClaims(token, isAccessToken);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}