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
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.exception.ErrorResponse;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final AuthRepository authRepository;
    private final JWTUtil jwtUtil;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final UserService userService;

    /**
     * 리프레시 토큰을 저장하거나 업데이트
     */
    @Transactional
    public void saveRefreshToken(UUID userUuid, String refreshToken, String provider, String providerId) {
        UserEntity user = userService.findByUserUuid(userUuid);
        String email = user.getEmail();

        try {
            // provider가 null인 경우 'local'로 기본 설정
            String safeProvider = Optional.ofNullable(provider).orElse("local");

            // 특정 프로바이더의 인증 정보 찾기 (없으면 새로 생성)
            AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), safeProvider)
                    .orElse(AuthEntity.builder()
                            .user(user)
                            .provider(safeProvider)
                            .build());

            // JWTUtil의 LONG_REFRESH_TOKEN_EXPIRE 값과 동일하게 설정
            LocalDateTime expirationTime = LocalDateTime.now(KST)
                    .plus(Duration.ofMillis(jwtUtil.getLONG_REFRESH_TOKEN_EXPIRE()));
            auth.updateRefreshToken(refreshToken, expirationTime);

            // providerId가 null이 아닌 경우에만 설정
            if (providerId != null) {
                auth.setProviderId(providerId);
            }

            authRepository.save(auth);

        } catch (BusinessException e) {
            log.warn("리프레시 토큰 저장 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 저장 중 오류 발생 - 이메일: {}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 리프레시 토큰 무효화 (로그아웃)
     */
    @Transactional
    public void revokeRefreshToken(UUID userUuid) {
        UserEntity user = userService.findByUserUuid(userUuid);
        String email = user.getEmail();

        try {
            Optional<AuthEntity> authOpt = authRepository.findByUserAndProvider(Optional.of(user), "local");

            if (authOpt.isEmpty()) {
                log.warn("리프레시 토큰 무효화 실패 - 사용자({})에 대한 인증 정보 없음", email);
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
            }

            AuthEntity auth = authOpt.get();
            auth.deactivate();
            authRepository.save(auth);

            log.info("리프레시 토큰 무효화 완료 - 이메일: {}", email);
        } catch (BusinessException e) {
            log.warn("리프레시 토큰 무효화 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 무효화 처리 중 알 수 없는 오류 발생 - 이메일: {}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 응답
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        try {
            // 리프레시 토큰 검증
            ErrorCode errorCode = jwtUtil.validateToken(refreshToken, false);
            if (errorCode != null) {
                log.warn("리프레시 토큰 검증 실패 - 유효하지 않은 토큰");
                throw new BusinessException(errorCode, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰에서 이메일 추출
            UUID userUuid = jwtUtil.getUserUuid(refreshToken, false);

            // 사용자 조회
            UserEntity user = userService.findByUserUuid(userUuid);
            String email = user.getEmail();
            // DB에서 리프레시 토큰 조회
            AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), "local")
                    .orElseThrow(() -> {
                        log.warn("리프레시 토큰 검증 실패 - 사용자({})에 대한 인증 정보 없음", email);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
                    });

            // DB에 저장된 토큰과 요청된 토큰 비교
            if (!refreshToken.equals(auth.getRefreshToken())) {
                log.warn("리프레시 토큰 검증 실패 - 토큰 불일치: {}", email);
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 리프레시 토큰 만료 여부 KST 기준으로 확인
            if (auth.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now(KST))) {
                log.warn("리프레시 토큰 검증 실패 - 만료된 토큰: {}", email);
                throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_TOKEN, "리프레시 토큰이 만료되었습니다.");
            }

            // 새로운 Access Token 생성
            List<String> roles = user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole().getName().getRoleName())
                    .collect(Collectors.toList());

            boolean keepLoggedIn = false; // 로그인 유지 여부 (프론트엔드에서 전달받을 수도 있음)
            String newAccessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles, keepLoggedIn);

            log.info("리프레시 토큰 검증 성공 - 새로운 액세스 토큰 발급 완료: {}", email);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getSHORT_ACCESS_TOKEN_EXPIRE())
                    .build();
        }
        catch (BusinessException e) {
            log.warn("리프레시 토큰 검증 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 중 알 수 없는 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}