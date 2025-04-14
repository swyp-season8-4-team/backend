package org.swyp.dessertbee.community.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.community.mate.entity.MateMember;
import org.swyp.dessertbee.community.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateMemberRepository extends JpaRepository<MateMember, Long> {

    @Query("SELECT u FROM UserEntity u " +
            "JOIN MateMember m ON u.id = m.userId " +
            "WHERE m.mateId = :mateId AND m.grade = 'CREATOR'")
    UserEntity findByMateId(@Param("mateId") Long mateId);

    @Modifying
    @Query("UPDATE MateMember m SET m.applyStatus = :applyStatus WHERE m.mateId = :mateId AND m.userId = :userId")
    void updateApplyStatus(MateApplyStatus applyStatus, Long mateId, Long userId);

    Optional<MateMember> findGradeByMateIdAndUserIdAndDeletedAtIsNull(Long mateId, Long userId);

    Optional<MateMember> findByMateIdAndUserIdAndDeletedAtIsNull(Long mateId, Long userId);

    Optional<MateMember> findByMateIdAndUserId(Long mateId, Long userId);


    //신청했는지 안했는지 확인하는 필드
    MateMember findByMateIdAndDeletedAtIsNullAndUserId(Long mateId, Long userId);

    List<MateMember> findByMateIdAndDeletedAtIsNullAndApplyStatus(Long mateId, MateApplyStatus applyStatus);

    @Query("SELECT m FROM MateMember m WHERE m.mateId = :mateId " +
            "AND m.deletedAt IS NULL " +
            "AND m.applyStatus = 'APPROVED' AND m.grade = 'NORMAL'")
    List<MateMember> findByMateIdAndDeletedAtIsNullAndApplyStatusAndGrade_Normal(Long mateId, MateApplyStatus mateApplyStatus, MateMemberGrade mateMemberGrade);

    @Query("SELECT COUNT(m) FROM MateMember  m WHERE m.mateId = :mateId AND m.deletedAt IS NULL")
    Long countByMateId(Long mateId);
}
