package org.swyp.dessertbee.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.swyp.dessertbee.auth.dto.login.LoginRequest;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.dto.logout.LogoutResponse;
import org.swyp.dessertbee.auth.dto.signup.SignUpRequest;
import org.swyp.dessertbee.auth.dto.passwordreset.PasswordResetRequest;
import org.swyp.dessertbee.auth.exception.AuthExceptions.*;

import java.util.UUID;

/**
 * 인증 관련 서비스 인터페이스
 */
public interface AuthService {
    /**
     * 리프레시 토큰을 저장하거나 업데이트
     * @param userUuid 사용자 uuid
     * @param refreshToken 리프레시 토큰
     * @param provider 인증 제공자 (local, kakao 등)
     * @param providerId 제공자별 식별자 (소셜 로그인의 경우)
     * @param request HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @param response HTTP 응답 객체 (디바이스 ID 쿠키 설정용)
     */
    void saveRefreshToken(UUID userUuid, String refreshToken, String provider, String providerId, HttpServletRequest request, HttpServletResponse response);

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰 발급
     * @param refreshToken 리프레시 토큰
     * @param request HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @return 새로운 액세스 토큰 응답
     */
    TokenResponse refreshAccessToken(String refreshToken, HttpServletRequest request);

    /**
     * 리프레시 토큰 무효화 (로그아웃)
     * @param userUuid 사용자 uuid
     */
    void revokeRefreshToken(UUID userUuid);

    /**
     * 회원가입 처리
     * @param request 회원가입 요청 정보
     * @param verificationToken 이메일 인증 토큰
     * @param httpRequest HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @param httpResponse HTTP 응답 객체 (디바이스 ID 쿠키 설정용)
     * @return 회원가입 결과
     * @throws InvalidVerificationTokenException 유효하지 않은 인증 토큰
     * @throws DuplicateEmailException 이메일 중복
     */
    LoginResponse signup(SignUpRequest request, String verificationToken, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 로그인 처리
     * @param request 로그인 요청 정보
     * @param httpRequest HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @param httpResponse HTTP 응답 객체 (디바이스 ID 쿠키 설정용)
     * @return 로그인 응답 정보
     * @throws InvalidCredentialsException 잘못된 인증 정보
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 비밀번호 재설정
     * @param request 비밀번호 재설정 요청
     * @param verificationToken 이메일 인증 토큰
     * @throws InvalidVerificationTokenException 유효하지 않은 인증 토큰
     */
    void resetPassword(PasswordResetRequest request, String verificationToken);

    /**
     * 로그아웃 처리
     * @param token 액세스 토큰
     * @param request HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @param response HTTP 응답 객체 (디바이스 ID 쿠키 삭제용)
     * @return 로그아웃 응답 정보
     */
    LogoutResponse logout(String token, HttpServletRequest request, HttpServletResponse response);

    /**
     * 개발 환경용 로그인 처리
     * @param request 로그인 요청 정보
     * @param httpRequest HTTP 요청 객체 (디바이스 ID 쿠키 읽기용)
     * @param httpResponse HTTP 응답 객체 (디바이스 ID 쿠키 설정용)
     * @return 로그인 응답 정보
     * @throws InvalidCredentialsException 잘못된 인증 정보
     */
    LoginResponse devLogin(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}