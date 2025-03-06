package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.dto.login.LoginRequest;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.logout.LogoutResponse;
import org.swyp.dessertbee.auth.dto.passwordreset.PasswordResetRequest;
import org.swyp.dessertbee.auth.dto.signup.SignUpRequest;
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;
import org.swyp.dessertbee.preference.service.PreferenceService;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.role.service.UserRoleService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final PreferenceService preferenceService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;

    @Override
    public TokenResponse refreshAccessToken(String refreshToken) {
        return tokenService.refreshAccessToken(refreshToken);
    }

    @Override
    public void saveRefreshToken(String email, String refreshToken) {
        tokenService.saveRefreshToken(email, refreshToken, "local", null);
    }

    @Override
    public void revokeRefreshToken(String email) {
        tokenService.revokeRefreshToken(email);
    }

    /**
     * 회원가입 처리
     */
    @Override
    @Transactional
    public LoginResponse signup(SignUpRequest request, String verificationToken) {
        try {
            // 메일 인증 토큰 검증
            validateEmailVerificationToken(verificationToken, request.getEmail(), EmailVerificationPurpose.SIGNUP);

            // 이메일 중복 검사
            if (userRepository.existsByEmail(request.getEmail())) {
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
            }
            else {
                roles = userRoleService.setUserRoles(user, Collections.singletonList(request.getRole()));
            }

            // 사용자 정보 저장
            userRepository.save(user);

            // Access Token, Refresh Token 생성
            String accessToken = jwtUtil.createAccessToken(user.getEmail(), roles, false);
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), roles, false);
            long expiresIn = jwtUtil.getSHORT_ACCESS_TOKEN_EXPIRE();


            // Refresh Token 저장
            saveRefreshToken(user.getEmail(), refreshToken);

            log.info("회원가입 완료 - 이메일: {}", request.getEmail());

            String profileImageUrl = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId()).stream().findFirst().orElse(null);

            return LoginResponse.success(accessToken, refreshToken, expiresIn, user, profileImageUrl);

        } catch (BusinessException e) {
            log.warn("회원가입 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 로그인 처리
     * 1. 사용자 인증
     * 2. JWT 토큰 생성
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 사용자 조회
            UserEntity user = userService.findUserByEmail(request.getEmail());

            // 2. 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("로그인 실패 - 비밀번호 불일치: {}", request.getEmail());
                throw new InvalidCredentialsException("비밀번호가 올바르지 않습니다.");
            }

            // 3. 사용자 권한 조회
            List<String> roles = userRoleService.getUserRoles(user);
            if (roles.isEmpty()) {
                // 역할이 비었다면 기본 역할 부여
                roles = userRoleService.ensureDefaultRole(user);
            }

            // 4. Access Token, Refresh Token 생성
            boolean keepLoggedIn = request.isKeepLoggedIn();
            String accessToken = jwtUtil.createAccessToken(user.getEmail(), roles, keepLoggedIn);
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), roles, keepLoggedIn);
            long expiresIn = keepLoggedIn ?
                    jwtUtil.getLONG_ACCESS_TOKEN_EXPIRE() :
                    jwtUtil.getSHORT_ACCESS_TOKEN_EXPIRE();

            // 5. Refresh Token 저장
            saveRefreshToken(user.getEmail(), refreshToken);

            // 6. 프로필 이미지
            String profileImageUrl = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId()).stream().findFirst().orElse(null);

            // 7. 선호도 설정 여부 파악
            boolean isPreferenceSet = preferenceService.isUserPreferenceSet(user);

            // 8. 로그인 응답 생성
            return LoginResponse.success(accessToken, refreshToken, expiresIn, user, profileImageUrl, isPreferenceSet);

        } catch (InvalidCredentialsException e) {
            log.warn("로그인 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 개발 환경용 로그인 처리
     * 일반 로그인과 동일하지만 토큰 유효 시간이 매우 짧음 (3~5분)
     */
    @Override
    @Transactional
    public LoginResponse devLogin(LoginRequest request) {
        try {
            // 1. 사용자 조회
            UserEntity user = userService.findUserByEmail(request.getEmail());

            // 2. 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("개발 로그인 실패 - 비밀번호 불일치: {}", request.getEmail());
                throw new InvalidCredentialsException("비밀번호가 올바르지 않습니다.");
            }

            // 3. 사용자 권한 조회
            List<String> roles = userRoleService.getUserRoles(user);
            if (roles.isEmpty()) {
                // 역할이 비었다면 기본 역할 부여
                roles = userRoleService.ensureDefaultRole(user);
            }

            // 4. 개발용 짧은 유효기간의 Access Token, Refresh Token 생성 (3~5분)
            String accessToken = jwtUtil.createDevAccessToken(user.getEmail(), roles);
            String refreshToken = jwtUtil.createDevRefreshToken(user.getEmail(), roles);

            // 개발 환경용 토큰 만료 시간 (3분 = 180초)
            long expiresIn = 180;

            // 5. Refresh Token 저장
            saveRefreshToken(user.getEmail(), refreshToken);

            // 6. 프로필 이미지
            List<String> profileImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());
            String profileImageUrl = profileImages.isEmpty() ? null : profileImages.get(0);

            // 7. 선호도 설정 여부 파악
            boolean isPreferenceSet = preferenceService.isUserPreferenceSet(user);

            log.info("개발 로그인 성공 - 이메일: {}, 토큰 만료시간: {}초", request.getEmail(), expiresIn);

            // 8. 로그인 응답 생성
            return LoginResponse.success(accessToken, refreshToken, expiresIn, user, profileImageUrl, isPreferenceSet);

        } catch (InvalidCredentialsException e) {
            log.warn("개발 로그인 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("개발 로그인 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new RuntimeException("개발 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 비밀번호 재설정
     * 이메일 인증이 완료된 사용자의 비밀번호 변경
     *
     * 1. 이메일 검증 토큰에서 사용자 이메일 추출
     * 2. 사용자 존재 확인
     * 3. 새 비밀번호 암호화 및 업데이트
     * 4. 기존 토큰 무효화 (로그아웃)
     */
    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request, String verificationToken) {
        try {

            // 토큰 유효성 검사
            validateEmailVerificationToken(verificationToken, request.getEmail(), EmailVerificationPurpose.PASSWORD_RESET);

            // 사용자 조회
            UserEntity user = userService.findUserByEmail(request.getEmail());

            // 새 비밀번호 암호화 및 저장
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 로그아웃 처리 (기존 토큰 무효화)
            revokeRefreshToken(request.getEmail());

            log.info("비밀번호 재설정 완료 - 이메일: {}", request.getEmail());

        } catch (BusinessException e) {
            log.warn("비밀번호 재설정 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 재설정 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 로그아웃 처리
     * 사용자의 리프레시 토큰을 무효화
     *
     * 1. 액세스 토큰에서 이메일 추출
     * 2. 리프레시 토큰 무효화
     *
     * @param token 액세스 토큰
     * @return 로그아웃 응답
     */
    @Transactional
    @Override
    public LogoutResponse logout(String token) {
        try {
            String email = jwtUtil.getEmail(token, true);

            revokeRefreshToken(email);

            log.info("로그아웃 성공 - 이메일: {}", email);
            return LogoutResponse.success();

        } catch (BusinessException e) {
            log.warn("로그아웃 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그아웃 처리 중 알 수 없는 오류 발생 - 토큰: {}", token, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateEmailVerificationToken(String token, String requestEmail, EmailVerificationPurpose expectedPurpose) {
        try {

            // 토큰 유효성 검사
            if (jwtUtil.validateToken(token, true) != null) {
                log.warn("토큰 검증 실패 - 만료되거나 유효하지 않은 인증 토큰: {}", token);
                throw new InvalidVerificationTokenException("만료되었거나 유효하지 않은 이메일 인증 토큰입니다.");
            }

            // 토큰에서 이메일 추출
            String verifiedEmail = jwtUtil.getEmail(token, true);
            if (!verifiedEmail.equals(requestEmail)) {
                log.warn("토큰 검증 실패 - 토큰의 이메일({})과 요청한 이메일({}) 불일치", verifiedEmail, requestEmail);
                throw new InvalidVerificationTokenException("인증된 이메일과 요청한 이메일이 일치하지 않습니다.");
            }

            // 토큰의 용도 확인
            EmailVerificationPurpose purpose = jwtUtil.getVerificationPurpose(token);
            if (purpose != expectedPurpose) {
                log.warn("토큰 검증 실패 - 예상된 목적({})과 토큰의 목적({}) 불일치", expectedPurpose, purpose);
                throw new InvalidVerificationTokenException("유효하지 않은 인증 토큰입니다.");
            }

            // 검증 성공 로그 추가
            log.info("토큰 검증 성공 - 이메일: {}, 용도: {}", verifiedEmail, expectedPurpose);

        } catch (InvalidVerificationTokenException e) {
            log.warn("이메일 인증 토큰 검증 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증 토큰 검증 중 알 수 없는 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
