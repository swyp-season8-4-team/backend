package org.swyp.dessertbee.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<AuthEntity, Integer> {
    /**
     * 사용자와 인증 제공자로 인증 정보 조회
     * @param user 사용자 엔티티
     * @param provider 인증 제공자 (예: local, google 등)
     * @return 해당 조건에 맞는 인증 정보
     */
    Optional<AuthEntity> findByUserAndProvider(Optional<UserEntity> user, String provider);

    /**
     * 사용자의 모든 인증 정보 조회
     * @param user 사용자 엔티티
     * @return 사용자의 모든 인증 정보 목록
     */
    List<AuthEntity> findAllByUser(UserEntity user);

    /**
     * 사용자, 공급자, 디바이스 ID로 인증 정보 찾기
     * @param user 사용자 엔티티
     * @param provider 인증 제공자 (예: local, google 등)
     * @param deviceId 디바이스 식별자
     * @return 해당 조건에 맞는 인증 정보
     */
    Optional<AuthEntity> findByUserAndProviderAndDeviceId(UserEntity user, String provider, String deviceId);

    /**
     * 사용자 UUID, 공급자, 디바이스 ID로 인증 정보 찾기
     * @param userUuid 사용자 UUID
     * @param provider 인증 제공자
     * @param deviceId 디바이스 식별자
     * @return 해당 조건에 맞는 인증 정보
     */
    Optional<AuthEntity> findByUser_UserUuidAndProviderAndDeviceId(UUID userUuid, String provider, String deviceId);

    /**
     * 특정 사용자 및 디바이스의 인증 정보 삭제 (로그아웃 시 사용)
     * @param userUuid 사용자 UUID
     * @param provider 인증 제공자
     * @param deviceId 디바이스 식별자
     */
    void deleteByUser_UserUuidAndProviderAndDeviceId(UUID userUuid, String provider, String deviceId);
}
