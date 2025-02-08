package org.swyp.dessertbee.auth.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 인증 관련 예외 클래스들을 모아둔 클래스
 */
public class AuthExceptions {

    /**
     * 이메일 중복 예외
     */
    public static class DuplicateEmailException extends BusinessException {
        public DuplicateEmailException() {
            super(ErrorCode.DUPLICATE_EMAIL);
        }

        public DuplicateEmailException(String message) {
            super(ErrorCode.DUPLICATE_EMAIL, message);
        }
    }

    /**
     * 닉네임 중복 예외
     */
    public static class DuplicateNicknameException extends BusinessException {
        public DuplicateNicknameException() {
            super(ErrorCode.DUPLICATE_NICKNAME);
        }

        public DuplicateNicknameException(String message) {
            super(ErrorCode.DUPLICATE_NICKNAME, message);
        }
    }

    /**
     * 인증 토큰 검증 실패 예외
     */
    public static class InvalidVerificationTokenException extends BusinessException {
        public InvalidVerificationTokenException() {
            super(ErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        public InvalidVerificationTokenException(String message) {
            super(ErrorCode.INVALID_VERIFICATION_TOKEN, message);
        }
    }

    /**
     * 인증 토큰 만료 예외
     */
    public static class ExpiredVerificationTokenException extends BusinessException {
        public ExpiredVerificationTokenException() {
            super(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }

        public ExpiredVerificationTokenException(String message) {
            super(ErrorCode.EXPIRED_VERIFICATION_TOKEN, message);
        }
    }

    /**
     * 비밀번호 불일치 예외
     */
    public static class PasswordMismatchException extends BusinessException {
        public PasswordMismatchException() {
            super(ErrorCode.PASSWORD_MISMATCH);
        }

        public PasswordMismatchException(String message) {
            super(ErrorCode.PASSWORD_MISMATCH, message);
        }
    }

    /**
     * 잘못된 인증 정보 예외
     * 로그인 실패, 인증 실패 등에 사용
     */
    public static class InvalidCredentialsException extends BusinessException {
        public InvalidCredentialsException() {
            super(ErrorCode.INVALID_CREDENTIALS);
        }

        public InvalidCredentialsException(String message) {
            super(ErrorCode.INVALID_CREDENTIALS, message);
        }
    }
}