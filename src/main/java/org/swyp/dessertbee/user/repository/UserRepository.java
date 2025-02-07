package org.swyp.dessertbee.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Optional;

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
}
