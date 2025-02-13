package org.swyp.dessertbee.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.email.entity.EmailVerificationEntity;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 특정 이메일의 최근 인증 요청 횟수를 조회
     * soft delete되지 않은 레코드만 계산
     * @param email 조회할 이메일 주소
     * @param since 조회 시작 시간
     * @return 해당 기간 동안의 인증 요청 횟수
     */
    @Query("SELECT COUNT(e) FROM EmailVerificationEntity e WHERE e.email = :email " +
            "AND e.createdAt > :since AND e.deletedAt IS NULL")
    long countRecentVerificationRequests(
            @Param("email") String email,
            @Param("since") LocalDateTime since
    );

    /**
     * 지정된 시간 이전의 레코드들을 soft delete 처리
     * 이미 soft delete된 레코드는 제외
     * @param before 이 시간 이전의 레코드들이 soft delete 처리되는 겁니다
     */
    @Modifying
    @Query("UPDATE EmailVerificationEntity e SET e.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE e.createdAt < :before AND e.deletedAt IS NULL")
    void softDeleteOldRecords(@Param("before") LocalDateTime before);

    /**
     * 인증되지 않고 삭제되지 않은 이메일 인증 정보 조회
     */
    List<EmailVerificationEntity> findByEmailAndPurposeAndVerifiedFalseAndDeletedAtIsNull(
            String email,
            EmailVerificationPurpose purpose
    );

}