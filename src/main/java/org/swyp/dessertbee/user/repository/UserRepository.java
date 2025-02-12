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
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return UserEntity
     */
    Optional<UserEntity> findByEmail(String email);
    /**
     * 이메일 존재 여부 확인
     * @param email 사용자 이메일
     * @return boolean
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     * @param nickname 사용자 닉네임
     * @return boolean
     */
    boolean existsByNickname(String nickname);

    /**
     * Uuid로 userId 조회
     * */
    @Query("SELECT u.id FROM UserEntity u where u.userUuid = :userUuid")
    Long findIdByUserUuid(UUID userUuid);

    /**
     * userId로 userUuid 조회
     * */
    @Query("SELECT u.userUuid FROM UserEntity u where u.id = :userId")
    UUID findUserUuidById(Long userId);


    /**
     * userId로 userUuid와 nickname 전체 조회
     * */
    @Query("SELECT u FROM UserEntity  u WHERE u.id = :userId")
    List<UserEntity> findAllUserUuidAndNicknameById(Long userId);
}
