package org.swyp.dessertbee.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Optional;
import java.util.List;

public interface AuthRepository extends JpaRepository<AuthEntity, Integer> {
    Optional<AuthEntity> findByUserAndProvider(Optional<UserEntity> user, String provider);

    /**
     * 사용자의 모든 인증 정보 조회
     * @param user 사용자 엔티티
     * @return 사용자의 모든 인증 정보 목록
     */
    List<AuthEntity> findAllByUser(UserEntity user);
}
