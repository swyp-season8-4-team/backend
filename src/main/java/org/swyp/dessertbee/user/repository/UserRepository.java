package org.swyp.dessertbee.user.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * 이메일 존재 여부 확인 (삭제된 계정 포함.)
     * @param email 사용자 이메일
     * @return boolean
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.email = :email")
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
    @Query("SELECT u.id FROM UserEntity u WHERE u.userUuid = :userUuid AND u.deletedAt IS NULL")
    Long findIdByUserUuid(UUID userUuid);


    Optional<Long> findOptionalIdByUserUuid(UUID userUuid);


    @Query("SELECT u.id FROM UserEntity u WHERE FUNCTION('UUID_TO_BIN', u.userUuid) = :userUuid AND u.deletedAt IS NULL")
    Long findIdByUserUuidUsingUuidToBin(UUID userUuid);

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


    @Query("SELECT u FROM UserEntity u WHERE u.userUuid = :userUuid AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUserUuid(@NotNull UUID userUuid);


    @Query("SELECT new org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto(u.userUuid, u.id, ur.role.id) " +
            "FROM UserEntity u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r")
    List<UserStatisticsResponseDto> findAllUsersWithRoles();

    // 특정 날짜(일)에 가입한 사용자 수 조회
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE DATE(u.createdAt) = :date")
    long countNewUsersByDay(LocalDate date);

    // 특정 주(일요일~토요일)에 가입한 사용자 수 조회
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :weekStart AND :weekEnd")
    long countNewUsersByWeek(LocalDate weekStart, LocalDate weekEnd);

    // 특정 월에 가입한 사용자 수 조회
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :monthStart AND :monthEnd")
    long countNewUsersByMonth(LocalDate monthStart, LocalDate monthEnd);
}
