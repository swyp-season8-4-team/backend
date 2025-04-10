package org.swyp.dessertbee.auth.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.swyp.dessertbee.auth.dto.response.PasswordResetResponse;
import org.swyp.dessertbee.auth.dto.response.TokenResponse;
import org.swyp.dessertbee.auth.dto.request.LoginRequest;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.dto.response.LogoutResponse;
import org.swyp.dessertbee.auth.dto.request.PasswordResetRequest;
import org.swyp.dessertbee.auth.dto.request.SignUpRequest;
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;
import org.swyp.dessertbee.email.service.EmailVerificationService;
import org.swyp.dessertbee.preference.service.PreferenceService;
import org.swyp.dessertbee.role.service.UserRoleService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final PreferenceService preferenceService;
    private final EmailVerificationService emailVerificationService;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;

    @Override
    public TokenResponse refreshAccessToken(String refreshToken, String deviceId) {
        return tokenService.refreshAccessToken(refreshToken, deviceId);
    }

    @Override
    public String saveRefreshToken(UUID userUuid, String refreshToken, String provider, String providerId, String deviceId) {
        return tokenService.saveRefreshToken(userUuid, refreshToken, provider, providerId, deviceId);
    }

    @Override
    public void revokeRefreshToken(UUID userUuid) {
        tokenService.revokeRefreshToken(userUuid);
    }

    /**
     * 회원가입 처리
     */
    @Override
    @Transactional
    public LoginResponse signup(SignUpRequest request, String verificationToken, String deviceId) {
        try {
            // 메일 인증 토큰 검증
            emailVerificationService.validateEmailVerificationToken(verificationToken, request.getEmail(), EmailVerificationPurpose.SIGNUP);

            // 이메일 중복 검사
            if (userService.isEmailExists(request.getEmail())) {
                throw new DuplicateEmailException("이미 등록된 이메일입니다.");
            }

            // 비밀번호 일치 여부 확인
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
            }

            // 닉네임 중복 검사
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicateNicknameException("이미 등록된 닉네임입니다.");
            }

            // UserEntity 생성
            UserEntity user = UserEntity.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .gender(request.getGender())
                    .build();

            // 역할 설정
            List<String> roles;
            if (request.getRole() == null) {
                roles = userRoleService.ensureDefaultRole(user);
            } else {
                roles = userRoleService.setUserRoles(user, Collections.singletonList(request.getRole()));
            }

            // 사용자 정보 저장
            userRepository.save(user);

            // Access Token, Refresh Token 생성
            String accessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);
            String refreshToken = jwtUtil.createRefreshToken(user.getUserUuid(), false);
            long expiresIn = jwtUtil.getACCESS_TOKEN_EXPIRE();

            // Refresh Token 저장 및 디바이스 ID 처리
            String usedDeviceId = saveRefreshToken(user.getUserUuid(), refreshToken, "local", null, deviceId);

            log.info("회원가입 완료 - 이메일: {}", request.getEmail());

            String profileImageUrl = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            return LoginResponse.success(accessToken, refreshToken, expiresIn, user, profileImageUrl, usedDeviceId);

        } catch (BusinessException e) {
            log.warn("회원가입 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new AuthServiceException("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그인 처리
     * 1. 사용자 인증
     * 2. 권한 확인 및 기본 역할 부여
     * 3. JWT 토큰 생성
     * 4. 리프레시 토큰 저장, 프로필 이미지 조회, 선호도 확인 후 응답 생성
     *
     * @param request 로그인 요청 정보
     * @param deviceId 디바이스 ID
     * @param isDev 개발 환경 로그인 여부
     * @return 로그인 응답
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String deviceId, boolean isDev) {
        try {

            // 계정 잠금 상태 확인
            loginAttemptService.checkLoginAttempt(request.getEmail());

            // 사용자 조회
            UserEntity user = userService.findUserByEmail(request.getEmail(), ErrorCode.INVALID_EMAIL);

            // 비밀번호 검증 및 실패 처리
            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("로그인 실패 - 비밀번호 인증 실패: {}", request.getEmail());

                // 로그인 실패 처리
                int remainingAttempts = loginAttemptService.handleLoginFailure(request.getEmail());

                // 남은 시도 횟수에 따라 다른 예외 발생
                if (remainingAttempts <= 0) {
                    throw new AccountLockedException();
                } else {
                    throw new InvalidPasswordException();
                }
            }

            // 인증 성공 시 실패 카운트 초기화
            loginAttemptService.resetFailedAttempts(request.getEmail());

            // 사용자 권한 확인 및 기본 역할 부여
            List<String> roles = determineUserRoles(user);

            // 토큰 생성 (개발 모드 여부에 따라 다른 토큰 생성)
            String accessToken;
            String refreshToken;
            long expiresIn;

            if (isDev) {
                // 개발용 짧은 유효기간 토큰
                accessToken = jwtUtil.createDevAccessToken(user.getUserUuid(), roles);
                refreshToken = jwtUtil.createDevRefreshToken(user.getUserUuid());
                expiresIn = 180; // 3분 = 180초
                log.info("개발 로그인 성공 - 이메일: {}, 토큰 만료시간: {}초", request.getEmail(), expiresIn);
            } else {
                // 일반 토큰
                boolean keepLoggedIn = request.isKeepLoggedIn();
                TokenPair tokenPair = createTokenPair(user, roles, keepLoggedIn);
                accessToken = tokenPair.accessToken();
                refreshToken = tokenPair.refreshToken();
                expiresIn = tokenPair.expiresIn();
            }

            // 리프레시 토큰 저장
            String usedDeviceId = saveRefreshToken(user, refreshToken, deviceId);

            // 프로필 이미지 조회
            String profileImageUrl = getProfileImage(user);

            // 선호도 설정 여부 확인
            boolean isPreferenceSet = preferenceService.isUserPreferenceSet(user);

            // 로그인 응답 생성
            return LoginResponse.success(
                    accessToken,
                    refreshToken,
                    expiresIn,
                    user,
                    profileImageUrl,
                    usedDeviceId,
                    isPreferenceSet
            );
        } catch (BusinessException e) {
            log.warn("로그인 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new AuthServiceException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 재설정
     * 이메일 인증이 완료된 사용자의 비밀번호 변경 후 기존 토큰 무효화
     */
    @Override
    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetRequest request, String verificationToken) {
        try {
            // 토큰 유효성 검사
            emailVerificationService.validateEmailVerificationToken(
                    verificationToken,
                    request.getEmail(),
                    EmailVerificationPurpose.PASSWORD_RESET
            );

            // 사용자 조회
            UserEntity user = userService.findUserByEmail(request.getEmail());

            // 계정 잠금 해제 (로그인 시도 카운터 초기화)
            loginAttemptService.unlockAccount(request.getEmail());

            // 새 비밀번호 암호화 및 저장
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 로그아웃 처리 (기존 토큰 무효화)
            revokeRefreshToken(user.getUserUuid());

            log.info("비밀번호 재설정 완료 - 이메일: {}", request.getEmail());
            return PasswordResetResponse.success();
        } catch (BusinessException e) {
            log.warn("비밀번호 재설정 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 재설정 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new AuthServiceException("비밀번호 재설정 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그아웃 처리 - 사용자의 리프레시 토큰 무효화
     */
    @Transactional
    @Override
    public LogoutResponse logout(String accessToken, String deviceId) {
        // 토큰이 없는 경우에도 로그아웃 성공으로 처리
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.info("토큰 없이 로그아웃 요청 - 성공으로 처리");
            return LogoutResponse.success();
        }

        try {
            // 액세스 토큰 파싱
            UUID userUuid = jwtUtil.getUserUuid(accessToken, true);

            try {
                if (deviceId != null && !deviceId.isEmpty()) {
                    // 특정 디바이스의 리프레시 토큰 무효화
                    tokenService.revokeRefreshTokenByDevice(userUuid, deviceId);
                    log.info("로그아웃 성공 - UUID: {}, 디바이스: {}", userUuid, deviceId);
                } else {
                    // 디바이스 ID가 없으면 모든 기기에서 로그아웃
                    tokenService.revokeRefreshToken(userUuid);
                    log.info("모든 기기에서 로그아웃 성공 - UUID: {}", userUuid);
                }
            } catch (Exception e) {
                // 리프레시 토큰 무효화 실패해도 로그아웃은 성공으로 처리
                log.warn("리프레시 토큰 무효화 실패 - UUID: {}, 사유: {}", userUuid, e.getMessage());
            }

        } catch (Exception e) {
            // 토큰 파싱 실패해도 로그아웃은 성공으로 처리
            log.warn("유효하지 않은 토큰으로 로그아웃 시도 - 성공으로 처리: {}", e.getMessage());
        }

        return LogoutResponse.success();
    }

    /**
     * 사용자 권한 조회 및 기본 역할 부여
     */
    private List<String> determineUserRoles(UserEntity user) {
        List<String> roles = userRoleService.getUserRoles(user);
        if (roles.isEmpty()) {
            roles = userRoleService.ensureDefaultRole(user);
        }
        return roles;
    }

    /**
     * Access Token, Refresh Token 및 만료시간 생성
     */
    private TokenPair createTokenPair(UserEntity user, List<String> roles, boolean keepLoggedIn) {
        String accessToken = jwtUtil.createAccessToken(user.getUserUuid(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getUserUuid(), keepLoggedIn);
        long expiresIn = jwtUtil.getACCESS_TOKEN_EXPIRE();
        return new TokenPair(accessToken, refreshToken, expiresIn);
    }

    /**
     * 리프레시 토큰 저장 및 디바이스 ID 처리
     */
    private String saveRefreshToken(UserEntity user, String refreshToken, String deviceId) {
        return tokenService.saveRefreshToken(user.getUserUuid(), refreshToken, "local", null, deviceId);
    }

    /**
     * 프로필 이미지 URL 조회
     */
    private String getProfileImage(UserEntity user) {
        return imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId())
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
         * 토큰 생성 결과를 담는 도메인 객체
         */
        private record TokenPair(String accessToken, String refreshToken, long expiresIn) {

    }
}
