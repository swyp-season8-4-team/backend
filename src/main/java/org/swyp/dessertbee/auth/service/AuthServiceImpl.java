package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * 인증 관련 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        AuthEntity auth = authRepository.findByUserAndProvider(user, "local")
                .orElse(AuthEntity.builder()
                        .user(user)
                        .provider("local")
                        .build());

        // 리프레시 토큰 만료 시간 설정 (14일)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

        auth.updateRefreshToken(refreshToken, expiresAt);
        authRepository.save(auth);

        log.info("Refresh token saved for user: {}", email);
    }

    @Override
    @Transactional
    public TokenResponse refreshAccessToken(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // DB에서 해당 유저의 리프레시 토큰 조회
        AuthEntity auth = authRepository.findByUserAndProvider(user, "local")
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        // 리프레시 토큰 유효성 검사
        if (!auth.isActive() ||
                auth.getRefreshToken() == null ||
                auth.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token is expired or invalid");
        }

        // 새로운 액세스 토큰 발급
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.createAccessToken(email, roles);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(1800) // 30분
                .build();
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
                        log.info("Refresh token revoked for user: {}", email);
                    });
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

}
