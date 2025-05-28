package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.response.TokenResponse;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;
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
     * 디바이스 ID 생성
     * @return 고유한 디바이스 ID
     */
    public String generateDeviceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 리프레시 토큰을 저장하거나 업데이트
     */
    @Transactional
    public String saveRefreshToken(UUID userUuid, String refreshToken, String provider, String providerId, String deviceId, boolean keepLoggedIn) {
        UserEntity user = userService.findByUserUuid(userUuid);
        String email = user.getEmail();

        try {
            // provider가 null인 경우 'local'로 기본 설정
            String safeProvider = Optional.ofNullable(provider).orElse("local");

            // 디바이스 ID가 없으면 새로 생성
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = generateDeviceId();
                log.debug("새 디바이스 ID 생성: {}", deviceId);
            }

            Optional<AuthEntity> authOpt = Optional.empty();

            // 소셜 로그인의 경우: provider/providerId 조합으로 먼저 조회
            if (providerId != null && !safeProvider.equals("local")) {
                log.debug("소셜 로그인 인증 정보 조회 - 제공자: {}, 제공자 ID: {}", safeProvider, providerId);
                authOpt = authRepository.findByProviderAndProviderId(safeProvider, providerId);
            }

            // 소셜 로그인으로 조회해도 없거나 일반 로그인인 경우: 사용자와 디바이스 ID로 조회
            if (authOpt.isEmpty()) {
                log.debug("사용자/디바이스 기반 인증 정보 조회 - 이메일: {}, 디바이스: {}", email, deviceId);
                authOpt = authRepository.findByUserAndProviderAndDeviceId(user, safeProvider, deviceId);
            }

            AuthEntity auth;
            if (authOpt.isPresent()) {
                // 기존 인증 정보가 있으면 리프레시 토큰 업데이트
                auth = authOpt.get();
            } else {
                // 없으면 새로 생성
                auth = AuthEntity.builder()
                        .user(user)
                        .provider(safeProvider)
                        .deviceId(deviceId)
                        .build();
            }

            long expireTimeMillis = keepLoggedIn ?
                    jwtUtil.getLONG_REFRESH_TOKEN_EXPIRE() :
                    jwtUtil.getSHORT_REFRESH_TOKEN_EXPIRE();

            LocalDateTime expirationTime = LocalDateTime.now(KST)
                    .plus(Duration.ofMillis(expireTimeMillis));

            auth.updateRefreshToken(refreshToken, expirationTime);

            // providerId가 null이 아닌 경우에만 설정
            if (providerId != null) {
                auth.updateProviderId(providerId);
            }

            authRepository.save(auth);

            // 생성하거나 사용한 디바이스 ID 반환
            return deviceId;

        } catch (BusinessException e) {
            log.warn("리프레시 토큰 저장 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 저장 중 오류 발생 - 이메일: {}", email, e);
            throw new AuthServiceException();
        }
    }

    /**
     * 특정 디바이스의 리프레시 토큰 무효화 (로그아웃)
     */
    @Transactional
    public void revokeRefreshTokenByDevice(UUID userUuid, String deviceId) {
        UserEntity user = userService.findByUserUuid(userUuid);
        String email = user.getEmail();

        try {
            // 디바이스 ID가 없는 경우 예외 발생
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("리프레시 토큰 무효화 실패 - 디바이스 ID가 제공되지 않음: {}", email);
                throw new JwtTokenException(ErrorCode.INVALID_CREDENTIALS, "디바이스 ID가 제공되지 않았습니다.");
            }

            // 특정 디바이스의 인증 정보만 찾기
            Optional<AuthEntity> authOpt = authRepository.findByUserAndDeviceId(user, deviceId);

            if (authOpt.isEmpty()) {
                log.warn("리프레시 토큰 무효화 실패 - 사용자({})의 디바이스({})에 대한 인증 정보 없음", email, deviceId);
                throw new JwtTokenException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
            }

            // 토큰 비활성화 처리
            AuthEntity auth = authOpt.get();
            auth.deactivate();
            authRepository.save(auth);

            log.info("리프레시 토큰 무효화 완료 - 이메일: {}, 디바이스: {}", email, deviceId);
        } catch (BusinessException e) {
            log.warn("리프레시 토큰 무효화 실패 - 이메일: {}, 디바이스: {}, 사유: {}", email, deviceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 무효화 처리 중 알 수 없는 오류 발생 - 이메일: {}, 디바이스: {}", email, deviceId, e);
            throw new AuthServiceException();
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
            List<AuthEntity> authEntities = authRepository.findAllByUser(user);

            if (authEntities.isEmpty()) {
                log.warn("리프레시 토큰 무효화 실패 - 사용자({})에 대한 인증 정보 없음", email);
                throw new JwtTokenException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
            }

            // 모든 인증 엔티티에 대해 토큰 비활성화 처리
            for (AuthEntity auth : authEntities) {
                auth.deactivate();
            }

            authRepository.saveAll(authEntities);

            log.info("리프레시 토큰 무효화 완료 - 이메일: {}", email);
        } catch (BusinessException e) {
            log.warn("리프레시 토큰 무효화 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 무효화 처리 중 알 수 없는 오류 발생 - 이메일: {}", email, e);
            throw new AuthServiceException();
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 응답
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken, String deviceId) {
        try {
            // 리프레시 토큰 검증
            ErrorCode errorCode = jwtUtil.validateToken(refreshToken, false);
            if (errorCode != null) {
                log.warn("리프레시 토큰 검증 실패 - 유효하지 않은 토큰");
                throw new JwtTokenException(errorCode, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰에서 사용자 UUID 추출
            UUID userUuid = jwtUtil.getUserUuid(refreshToken, false);

            // 사용자 조회
            UserEntity user = userService.findByUserUuid(userUuid);
            String email = user.getEmail();

            // 정지 여부 확인
            if (user.isSuspended()) {
                throw new AccountLockedException("계정이 정지되었습니다. 정지 해제 일시: " + user.getSuspendedUntil());
            }

            // 디바이스 ID 확인
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("리프레시 토큰 검증 실패 - 디바이스 ID 없음: {}", email);
                throw new DeviceIdMissingException();
            }

            // 디바이스 ID로 인증 정보 조회
            AuthEntity auth = authRepository.findByUserAndDeviceId(user, deviceId)
                    .orElseThrow(() -> {
                        log.warn("리프레시 토큰 검증 실패 - 사용자({})의 디바이스({})에 대한 인증 정보 없음", email, deviceId);
                        return new JwtTokenException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
                    });

            // 토큰의 활성화 상태 확인
            if (!auth.isActive()) {
                log.warn("리프레시 토큰 검증 실패 - 비활성화된 토큰: {}, 디바이스: {}", email, deviceId);
                throw new JwtTokenException(ErrorCode.INVALID_CREDENTIALS, "로그아웃 처리된 토큰입니다.");
            }

            // DB에 저장된 토큰과 요청된 토큰 비교
            if (!refreshToken.equals(auth.getRefreshToken())) {
                log.warn("리프레시 토큰 검증 실패 - 토큰 불일치: {}, 디바이스: {}", email, deviceId);
                throw new JwtTokenException(ErrorCode.INVALID_VERIFICATION_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 새로운 Access Token 생성
            List<String> roles = user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole().getName().getRoleName())
                    .collect(Collectors.toList());

            String newAccessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);

            // 마지막 로그인 시간 업데이트
            auth.updateRefreshToken(auth.getRefreshToken(), auth.getRefreshTokenExpiresAt());
            authRepository.save(auth);

            log.info("리프레시 토큰 검증 성공 - 새로운 액세스 토큰 발급 완료: {}, 디바이스: {}", email, deviceId);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getACCESS_TOKEN_EXPIRE())
                    .deviceId(deviceId)  // 응답에 디바이스 ID 포함
                    .build();
        }
        catch (BusinessException e) {
            log.warn("리프레시 토큰 검증 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 중 알 수 없는 오류 발생", e);
            throw new AuthServiceException();
        }
    }
}