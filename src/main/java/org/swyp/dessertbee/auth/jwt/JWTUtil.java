package org.swyp.dessertbee.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * JWT 토큰 생성 (이메일과 여러 역할 포함)
     */
    public String createJwt(String email, List<String> roles, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .claim("roles", roles) // 리스트 형태로 저장
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 이메일(email) 추출
     */
    public String getEmail(String token) {
        return parseTokenPayload(token).get("email", String.class);
    }

    /**
     * 역할(role) 리스트 추출
     */
    public List<String> getRoles(String jwtToken) {
        List<?> rawRoles = parseTokenPayload(jwtToken).get("roles", List.class);
        return rawRoles != null ? rawRoles.stream().map(Object::toString).toList() : List.of();
    }

    /**
     * JWT 페이로드(Claims) 파싱
     */
    private Claims parseTokenPayload(String jwtToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public Boolean isExpired(String token) {
        return parseTokenPayload(token).getExpiration().before(new Date());
    }
}
