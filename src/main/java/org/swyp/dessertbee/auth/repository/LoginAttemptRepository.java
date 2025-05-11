package org.swyp.dessertbee.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.auth.entity.LoginAttemptEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttemptEntity, Long> {

    /**
     * 이메일로 로그인 시도 정보 조회
     */
    Optional<LoginAttemptEntity> findByEmail(String email);

    /**
     * 이메일의 시도 정보 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 특정 시간 이전에 계정 잠금이 만료되는 레코드 조회
     */
    List<LoginAttemptEntity> findByLockedUntilBefore(LocalDateTime time);
}