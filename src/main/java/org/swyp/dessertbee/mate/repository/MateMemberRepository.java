package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Repository
public interface MateMemberRepository extends JpaRepository<MateMember, Long> {

    List<MateMember> findByMateIdAndDeletedAtIsNullAndApprovalYnTrue(Long mateId);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN MateMember m ON u.id = m.userId " +
            "WHERE m.mateId = :mateId AND m.grade = 'CREATOR'")
    UserEntity findByMateId(@Param("mateId") Long mateId);

    @Modifying
    @Query("UPDATE MateMember m SET m.approvalYn = true WHERE m.mateId = :mateId AND m.userId = :userId")
    void updateApprovalYn(Long mateId, Long userId);

    MateMember findByMateIdAndUserId(Long mateId, Long userId);

    MateMember findGradeByMateIdAndUserId(Long mateId, Long userId);
}
