package org.swyp.dessertbee.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.email.entity.EmailVerificationEntity;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    /**
     * 이메일, 목적으로 가장 최근 검증 정보 조회
     */
    Optional<EmailVerificationEntity> findFirstByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            EmailVerificationPurpose purpose
    );

    /**
     * 이메일, 코드, 목적으로 검증 정보 조회
     */
    Optional<EmailVerificationEntity> findByEmailAndCodeAndPurpose(
            String email,
            String code,
            EmailVerificationPurpose purpose
    );
}