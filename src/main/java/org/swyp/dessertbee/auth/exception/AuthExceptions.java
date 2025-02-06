package org.swyp.dessertbee.auth.exception;

public class AuthExceptions {

    /**
     * 인증 토큰 검증 실패시 발생하는 예외
     */
    public static class InvalidVerificationTokenException extends RuntimeException {
        public InvalidVerificationTokenException(String message) {
            super(message);
        }
    }

    /**
     * 비밀번호 검증 실패시 발생하는 예외
     */
    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    /**
     * 이메일 중복시 발생하는 예외
     */
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    /**
     * 잘못된 인증 정보 제공시 발생하는 예외
     */
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}