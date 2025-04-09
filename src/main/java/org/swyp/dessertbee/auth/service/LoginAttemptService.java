package org.swyp.dessertbee.auth.service;

/**
 * 로그인 시도 관리 서비스
 */
public interface LoginAttemptService {

    /**
     * 로그인 시도 전 계정 잠금 상태 확인
     * @param email 사용자 이메일
     * @throws org.swyp.dessertbee.auth.exception.AuthExceptions.AccountLockedException 계정이 잠긴 경우
     */
    void checkLoginAttempt(String email);

    /**
     * 로그인 실패 시 실패 횟수 증가
     * @param email 사용자 이메일
     * @return 남은 시도 횟수
     */
    int handleLoginFailure(String email);

    /**
     * 로그인 성공 시 실패 횟수 초기화 빵회로
     * @param email 사용자 이메일
     */
    void resetFailedAttempts(String email);

    /**
     * 비밀번호 재설정 등으로 계정 잠금 해제
     * @param email 사용자 이메일
     */
    void unlockAccount(String email);
}