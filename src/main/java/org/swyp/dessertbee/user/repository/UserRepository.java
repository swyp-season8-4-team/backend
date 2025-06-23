package org.swyp.dessertbee.user.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 이메일로 사용자 조회 (삭제되지 않은 계정만)
     *
     * @param email 사용자 이메일
     * @return UserEntity
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인 (삭제된 계정 포함.)
     *
     * @param email 사용자 이메일
     * @return boolean
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email")
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인 (삭제되지 않은 계정만)
     *
     * @param nickname 사용자 닉네임
     * @return boolean
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.nickname = :nickname")
    boolean existsByNickname(String nickname);

    /**
     * Uuid로 userId 조회 (삭제되지 않은 계정만)
     */
    @Query("SELECT u.id FROM UserEntity u WHERE u.userUuid = :userUuid AND u.deletedAt IS NULL")
    Long findIdByUserUuid(UUID userUuid);


    Optional<Long> findOptionalIdByUserUuid(UUID userUuid);


    @Query("SELECT u.id FROM UserEntity u WHERE FUNCTION('UUID_TO_BIN', u.userUuid) = :userUuid AND u.deletedAt IS NULL")
    Long findIdByUserUuidUsingUuidToBin(UUID userUuid);

    /**
     * userId로 userUuid 조회 (삭제되지 않은 계정만)
     */
    @Query("SELECT u.userUuid FROM UserEntity u WHERE u.id = :userId AND u.deletedAt IS NULL")
    UUID findUserUuidById(Long userId);

    /**
     * userId로 userUuid와 nickname 전체 조회
     */
    List<UserEntity> findAllUserUuidAndNicknameById(Long userId);


    /**
     * 삭제된 계정 중 이메일로 조회
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NOT NULL")
    Optional<UserEntity> findDeletedAccountByEmail(String email);


    @Query("SELECT u FROM UserEntity u WHERE u.userUuid = :userUuid AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUserUuid(@NotNull UUID userUuid);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long userId);

    @Query("SELECT u FROM UserEntity u WHERE u.id = :userId") // deletedAt 조건 없음
    Optional<UserEntity> findByIdIncludingDeleted(@Param("userId") Long userId);

    /**
     * 이메일과 OAuth 제공자로 사용자 조회 (auth 테이블과 조인)
     * 특정 OAuth 제공자로 가입한 사용자를 찾을 때 사용
     *
     * @param email 사용자 이메일
     * @param provider OAuth 제공자 (apple, kakao 등)
     * @return UserEntity
     */
    @Query("SELECT u FROM UserEntity u " +
           "JOIN u.authEntities a " +
           "WHERE u.email = :email AND a.provider = :provider AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmailAndOAuthProvider(@Param("email") String email, @Param("provider") String provider);

    /**
     * 이메일로 가입된 모든 OAuth 제공자 조회
     * 사용자가 어떤 OAuth 제공자로 가입했는지 확인할 때 사용
     *
     * @param email 사용자 이메일
     * @return OAuth 제공자 목록
     */
    @Query("SELECT DISTINCT a.provider FROM UserEntity u " +
           "JOIN u.authEntities a " +
           "WHERE u.email = :email AND u.deletedAt IS NULL")
    List<String> findOAuthProvidersByEmail(@Param("email") String email);

    /**
     * 이메일로 가입된 OAuth 계정 수 조회
     * 사용자가 여러 OAuth 제공자로 가입했는지 확인할 때 사용
     *
     * @param email 사용자 이메일
     * @return OAuth 계정 수
     */
    @Query("SELECT COUNT(DISTINCT a.provider) FROM UserEntity u " +
           "JOIN u.authEntities a " +
           "WHERE u.email = :email AND u.deletedAt IS NULL")
    long countOAuthProvidersByEmail(@Param("email") String email);
}
