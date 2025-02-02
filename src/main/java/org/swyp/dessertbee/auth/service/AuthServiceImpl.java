package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JWTUtil jwtUtil;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken, false)) {
            return null;
        }

        String email = jwtUtil.getEmail(refreshToken);
        List<String> roles = jwtUtil.getRoles(refreshToken);

        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }

        AuthEntity auth = authRepository.findByUserAndProvider(user, "local")
                .orElse(null);

        if (auth == null || !auth.isActive() ||
                !refreshToken.equals(auth.getRefreshToken()) ||
                auth.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        // 새로운 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(email, roles);
        String newRefreshToken = jwtUtil.createRefreshToken(email, roles);

        // Refresh Token 업데이트 (RTR 기법 적용)
        auth.updateRefreshToken(
                newRefreshToken,
                LocalDateTime.now().plusDays(14)
        );
        authRepository.save(auth);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(1800) // 30분
                .build();
    }

    @Override
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        UserEntity user = userRepository.findByEmail(email);
        if (user != null) {
            AuthEntity auth = authRepository.findByUserAndProvider(user, "local")
                    .orElse(AuthEntity.builder()
                            .user(user)
                            .provider("local")
                            .build());

            auth.updateRefreshToken(
                    refreshToken,
                    LocalDateTime.now().plusDays(14)
            );
            authRepository.save(auth);
        }
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user != null) {
            authRepository.findByUserAndProvider(user, "local")
                    .ifPresent(auth -> {
                        auth.deactivate();
                        authRepository.save(auth);
                    });
        }
    }
}
