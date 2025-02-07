package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    /**
     * 리프레시 토큰을 저장하거나 업데이트
     */
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), "local")
                .orElse(AuthEntity.builder()
                        .user(user)
                        .provider("local")
                        .build());

        auth.updateRefreshToken(refreshToken, LocalDateTime.now().plusDays(14));
        authRepository.save(auth);
    }

    /**
     * 리프레시 토큰 무효화 (로그아웃)
     */
    @Transactional
    public void revokeRefreshToken(String email) {
        userRepository.findByEmail(email)
                .flatMap(user -> authRepository.findByUserAndProvider(Optional.of(user), "local"))
                .ifPresent(auth -> {
                    auth.deactivate();
                    authRepository.save(auth);
                });
    }

    @Transactional
    public TokenResponse refreshAccessToken(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), "local")
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!auth.isActive() ||
                auth.getRefreshToken() == null ||
                auth.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token is expired or invalid");
        }

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.createAccessToken(email, roles, false);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(1800)
                .build();
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}