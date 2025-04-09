package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.entity.LoginAttemptEntity;
import org.swyp.dessertbee.auth.exception.AuthExceptions.AccountLockedException;
import org.swyp.dessertbee.auth.repository.LoginAttemptRepository;

import java.util.Optional;

/**
 * 계정 잠금 정책 및 로그인 시도 관리 로직 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    // 계정 잠금 횟수
    private static final int MAX_FAILED_ATTEMPTS = 5;
    // 계정 잠금 시간(분)
    private static final int LOCK_TIME_MINUTES = 10;

    /**
     * 로그인 시도 전 계정 잠금 상태 확인
     * @param email 사용자 이메일
     * @throws AccountLockedException 계정이 잠긴 경우
     */
    @Override
    @Transactional(readOnly = true)
    public void checkLoginAttempt(String email) {
        Optional<LoginAttemptEntity> attemptOpt = loginAttemptRepository.findByEmail(email);

        // 존재하지 않으면 잠금 상태 아님
        if (attemptOpt.isEmpty()) {
            return;
        }

        LoginAttemptEntity loginAttempt = attemptOpt.get();

        if (loginAttempt.isAccountLocked()) {
            long remainingMinutes = loginAttempt.getRemainingLockTime();
            log.warn("계정 잠금 상태 - 이메일: {}, 남은 시간: {}분", email, remainingMinutes);
            throw new AccountLockedException("비밀번호 입력 횟수 초과하였습니다. [" +
                    remainingMinutes + "분]뒤 재시도 가능합니다. 비밀번호 찾기를 이용하여 비밀번호를 변경 하신 후 사용하시기 바랍니다.");
        }
    }

    /**
     * 로그인 실패 시 실패 횟수 증가 및 남은 시도 횟수 반환
     * @param email 사용자 이메일
     * @return 남은 로그인 시도 횟수
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int incrementFailedAttempts(String email) {
        LoginAttemptEntity loginAttempt = getOrCreateLoginAttempt(email);
        loginAttempt.incrementFailedAttempts();

        // 저장 전후로 로그 추가
        log.info("로그인 실패 저장 전 - 이메일: {}, 실패 횟수: {}", email, loginAttempt.getFailedAttempts());
        LoginAttemptEntity saved = loginAttemptRepository.save(loginAttempt);
        log.info("로그인 실패 저장 후 - 이메일: {}, 실패 횟수: {}, ID: {}",
                email, saved.getFailedAttempts(), saved.getId());

        // 변경 내용 즉시 반영
        loginAttemptRepository.flush();

        int remainingAttempts = MAX_FAILED_ATTEMPTS - loginAttempt.getFailedAttempts();
        log.info("로그인 실패 - 이메일: {}, 남은 시도 횟수: {}", email, remainingAttempts);

        return Math.max(0, remainingAttempts);
    }

    /**
     * 로그인 성공 시 실패 카운터 초기화
     * @param email 사용자 이메일
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(String email) {
        LoginAttemptEntity loginAttempt = getOrCreateLoginAttempt(email);
        loginAttempt.resetFailedAttempts();
        loginAttemptRepository.save(loginAttempt);
        log.info("로그인 성공 - 이메일: {}, 로그인 시도 카운터 초기화", email);
    }

    /**
     * 비밀번호 재설정으로 계정 잠금 해제
     * @param email 사용자 이메일
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlockAccount(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(loginAttempt -> {
            loginAttempt.resetFailedAttempts();
            loginAttemptRepository.save(loginAttempt);
            log.info("계정 잠금 해제 - 이메일: {}, 비밀번호 재설정으로 인한 잠금 해제", email);
        });
    }

    /**
     * 이메일로 로그인 시도 엔티티 조회, 없으면 새로 생성
     * @param email 사용자 이메일
     * @return 로그인 시도 엔티티
     */
    protected LoginAttemptEntity getOrCreateLoginAttempt(String email) {
        // 먼저 email로 조회
        Optional<LoginAttemptEntity> existingAttempt = loginAttemptRepository.findByEmail(email);

        if (existingAttempt.isPresent()) {
            return existingAttempt.get();
        }

        // 존재하지 않는 경우 새로 생성
        LoginAttemptEntity newAttempt = LoginAttemptEntity.builder()
                .email(email)
                .failedAttempts(0)
                .build();
        return loginAttemptRepository.save(newAttempt);
    }

}