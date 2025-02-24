package org.swyp.dessertbee.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 이메일로 사용자 조회 (삭제되지 않은 계정만)
     * @param email 사용자 이메일
     * @return UserEntity
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmail(String email);
    /**
     * 이메일 존재 여부 확인 (삭제되지 않은 계정만)
     * @param email 사용자 이메일
     * @return boolean
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인 (삭제되지 않은 계정만)
     * @param nickname 사용자 닉네임
     * @return boolean
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    boolean existsByNickname(String nickname);

    /**
     * Uuid로 userId 조회 (삭제되지 않은 계정만)
     * */
    @Query("SELECT u.id FROM UserEntity u WHERE FUNCTION('UUID_TO_BIN', u.userUuid) = :userUuid AND u.deletedAt IS NULL")
    /* @Query("SELECT u.id FROM UserEntity u WHERE u.userUuid = :userUuid AND u.deletedAt IS NULL") */
    Long findIdByUserUuid(UUID userUuid);

    /**
     * userId로 userUuid 조회 (삭제되지 않은 계정만)
     * */
    @Query("SELECT u.userUuid FROM UserEntity u WHERE u.id = :userId AND u.deletedAt IS NULL")
    UUID findUserUuidById(Long userId);

    /**
     * userId로 userUuid와 nickname 전체 조회
     * */
    List<UserEntity> findAllUserUuidAndNicknameById(Long userId);


    /**
     * 삭제된 계정 중 이메일로 조회
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NOT NULL")
    Optional<UserEntity> findDeletedAccountByEmail(String email);

}
