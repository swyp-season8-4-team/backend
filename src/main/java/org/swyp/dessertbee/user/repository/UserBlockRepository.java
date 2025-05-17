package org.swyp.dessertbee.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.user.entity.UserBlock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    /**
     * UUID 기반: 특정 사용자가 차단한 사용자 목록 조회
     */
    @Query("SELECT ub FROM UserBlock ub JOIN UserEntity blocker ON ub.blockerUserId = blocker.id WHERE blocker.userUuid = :blockerUuid")
    List<UserBlock> findByBlockerUuid(@Param("blockerUuid") UUID blockerUuid);

    /**
     * UUID 기반: 특정 사용자가 차단한 사용자 ID 목록 조회
     */
    @Query("SELECT ub.blockedUserId FROM UserBlock ub JOIN UserEntity blocker ON ub.blockerUserId = blocker.id WHERE blocker.userUuid = :blockerUuid")
    List<Long> findBlockedUserIdsByBlockerUuid(@Param("blockerUuid") UUID blockerUuid);

    /**
     * UUID 기반: 특정 사용자가 차단한 사용자 UUID 목록 조회
     */
    @Query("SELECT blocked.userUuid FROM UserBlock ub JOIN UserEntity blocker ON ub.blockerUserId = blocker.id JOIN UserEntity blocked ON ub.blockedUserId = blocked.id WHERE blocker.userUuid = :blockerUuid")
    List<UUID> findBlockedUserUuidsByBlockerUuid(@Param("blockerUuid") UUID blockerUuid);

    /**
     * UUID 기반: 특정 사용자의 차단 여부 확인 (blockerUuid가 blockedUuid를 차단했는지)
     */
    @Query("SELECT COUNT(ub) > 0 FROM UserBlock ub JOIN UserEntity blocker ON ub.blockerUserId = blocker.id JOIN UserEntity blocked ON ub.blockedUserId = blocked.id WHERE blocker.userUuid = :blockerUuid AND blocked.userUuid = :blockedUuid")
    boolean existsByBlockerUuidAndBlockedUuid(@Param("blockerUuid") UUID blockerUuid, @Param("blockedUuid") UUID blockedUuid);

    /**
     * UUID 기반: 특정 사용자가 특정 사용자를 차단한 내역 조회
     */
    @Query("SELECT ub FROM UserBlock ub JOIN UserEntity blocker ON ub.blockerUserId = blocker.id JOIN UserEntity blocked ON ub.blockedUserId = blocked.id WHERE blocker.userUuid = :blockerUuid AND blocked.userUuid = :blockedUuid")
    Optional<UserBlock> findByBlockerUuidAndBlockedUuid(@Param("blockerUuid") UUID blockerUuid, @Param("blockedUuid") UUID blockedUuid);

    /**
     * ID 기반: 특정 사용자가 차단한 사용자 목록 조회
     */
    List<UserBlock> findByBlockerUserId(Long blockerUserId);

    /**
     * ID 기반: 특정 사용자가 차단한 사용자 ID 목록 조회
     */
    @Query("SELECT ub.blockedUserId FROM UserBlock ub WHERE ub.blockerUserId = :blockerUserId")
    List<Long> findBlockedUserIdsByBlockerUserId(@Param("blockerUserId") Long blockerUserId);

    /**
     * ID 기반: 특정 사용자의 차단 여부 확인 (blockerUserId가 blockedUserId를 차단했는지)
     */
    boolean existsByBlockerUserIdAndBlockedUserId(Long blockerUserId, Long blockedUserId);

    /**
     * ID 기반: 특정 사용자가 특정 사용자를 차단한 내역 조회
     */
    Optional<UserBlock> findByBlockerUserIdAndBlockedUserId(Long blockerUserId, Long blockedUserId);
}