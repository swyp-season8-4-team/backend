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
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 리프레시 토큰을 저장하거나 업데이트
     */
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        try {

            // 사용자 조회
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("리프레시 토큰 저장 실패 - 존재하지 않는 사용자: {}", email);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "해당 이메일의 사용자를 찾을 수 없습니다.");
                    });

            AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), "local")
                    .orElse(AuthEntity.builder()
                            .user(user)
                            .provider("local")
                            .build());

            long refreshTokenExpireTime = 864000000; // 추후 변경
            auth.updateRefreshToken(refreshToken, LocalDateTime.now().plusSeconds(refreshTokenExpireTime));
            // JWTUtil의 LONG_REFRESH_TOKEN_EXPIRE 값과 동일하게 설정
            LocalDateTime expirationTime = LocalDateTime.now(KST)
                    .plus(Duration.ofMillis(jwtUtil.getLONG_REFRESH_TOKEN_EXPIRE()));
            auth.updateRefreshToken(refreshToken, expirationTime);

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
    public void revokeRefreshToken(String email) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                log.warn("리프레시 토큰 무효화 실패 - 존재하지 않는 사용자: {}", email);
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "해당 이메일의 사용자를 찾을 수 없습니다.");
            }

            UserEntity user = userOpt.get();

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


    @Transactional
    public TokenResponse refreshAccessToken(String email) {
        try {
            // 사용자 조회
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("리프레시 토큰 검증 실패 - 존재하지 않는 사용자: {}", email);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "해당 이메일의 사용자를 찾을 수 없습니다.");
                    });

            // DB에서 리프레시 토큰 조회 및 검증
            AuthEntity auth = authRepository.findByUserAndProvider(Optional.of(user), "local")
                    .orElseThrow(() -> {
                        log.warn("리프레시 토큰 검증 실패 - 사용자({})에 대한 인증 정보 없음", email);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "리프레시 토큰이 존재하지 않습니다.");
                    });

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
            String newAccessToken = jwtUtil.createAccessToken(email, roles, keepLoggedIn);

            log.info("리프레시 토큰 검증 성공 - 새로운 액세스 토큰 발급 완료: {}", email);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getSHORT_ACCESS_TOKEN_EXPIRE())
                    .build();
        }
        catch (BusinessException e) {
            log.warn("리프레시 토큰 검증 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 중 알 수 없는 오류 발생 - 이메일: {}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}