package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.dto.login.LoginRequest;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.logout.LogoutResponse;
import org.swyp.dessertbee.auth.dto.passwordreset.PasswordResetRequest;
import org.swyp.dessertbee.auth.dto.signup.SignUpRequest;
import org.swyp.dessertbee.auth.dto.signup.SignUpResponse;
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

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

    @Autowired
    private TokenService tokenService;

    @Override
    public TokenResponse refreshAccessToken(String email) {
        return tokenService.refreshAccessToken(email);
    }

    @Override
    public void saveRefreshToken(String email, String refreshToken) {
        tokenService.saveRefreshToken(email, refreshToken);
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
    public SignUpResponse signup(SignUpRequest request, String verificationToken) {
        try {
            // 1. 이메일 인증 토큰 검증
            String verifiedEmail = jwtUtil.getEmail(verificationToken, true);
            EmailVerificationPurpose purpose = jwtUtil.getVerificationPurpose(verificationToken);

            if (!jwtUtil.validateToken(verificationToken, true)) {
                throw new InvalidVerificationTokenException("만료되거나 유효하지 않은 인증 토큰입니다.");
            }

            // 2. 토큰의 이메일과 요청의 이메일이 일치하는지 확인
            if (!verifiedEmail.equals(request.getEmail())) {
                throw new InvalidVerificationTokenException("인증된 이메일과 요청한 이메일이 일치하지 않습니다.");
            }

            // 3. 인증 토큰의 목적 확인
            if (purpose != EmailVerificationPurpose.SIGNUP) {
                throw new InvalidVerificationTokenException("유효하지 않은 인증 토큰입니다.");
            }

            // 4. 이메일 중복 검사
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("이미 등록된 이메일입니다.");
            }

            // 5. 비밀번호 일치 여부 확인
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
            }

            // 6. UserEntity 생성
            UserEntity user = UserEntity.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .gender(request.getGender())
                    .build();

            // 7. 기본 사용자 권한(ROLE_USER) 설정
            RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("기본 권한이 설정되어 있지 않습니다."));
            user.addRole(userRole);

            // 8. 사용자 정보 저장
            userRepository.save(user);
            log.info("회원가입 완료 - 이메일: {}", request.getEmail());

            // 9. 응답 생성
            return SignUpResponse.success(request.getEmail());

        } catch (Exception e) {
            log.error("회원가입 실패 - 이메일: {}, 에러: {}", request.getEmail(), e.getMessage());
            throw e;
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
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

            // 2. 비밀번호 검증
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
            }

            // 3. 사용자 권한 조회
            List<String> roles = user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole().getName())
                    .collect(Collectors.toList());

            // 4. Access Token, Refresh Token 생성
            String accessToken = jwtUtil.createAccessToken(user.getEmail(), roles, request.isKeepLoggedIn());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), roles, request.isKeepLoggedIn());

            // 5. Refresh Token 저장
            saveRefreshToken(user.getEmail(), refreshToken);

            // 7. 로그인 응답 생성
            return LoginResponse.success(accessToken, user);

        } catch (InvalidCredentialsException e) {
            log.warn("로그인 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
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
        // 1. 이메일 검증 토큰에서 이메일 추출
        String verifiedEmail = jwtUtil.getEmail(verificationToken, true);

        // 2. 사용자 조회
        UserEntity user = userRepository.findByEmail(verifiedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 3. 새 비밀번호 암호화 및 저장
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. 로그아웃 처리 (기존 토큰 무효화)
        revokeRefreshToken(verifiedEmail);

        log.info("비밀번호 재설정 완료 - 이메일: {}", verifiedEmail);
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
        String email = jwtUtil.getEmail(token, true);
        revokeRefreshToken(email);
        return LogoutResponse.success();
    }
}
