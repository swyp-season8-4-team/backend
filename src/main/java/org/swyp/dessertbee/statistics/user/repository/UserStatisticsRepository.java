package org.swyp.dessertbee.statistics.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.swyp.dessertbee.statistics.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface UserStatisticsRepository  extends Repository<UserEntity, Long> {

    @Query("SELECT new org.swyp.dessertbee.statistics.common.dto.user.response.UserStatisticsResponseDto(u.userUuid, u.id, ur.role.id) " +
            "FROM UserEntity u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r")
    List<UserStatisticsResponseDto> findAllUsersWithRoles();

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countNewUsersByDay(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :weekStart AND :weekEnd")
    long countNewUsersByWeek(@Param("weekStart") LocalDateTime weekStart,
                             @Param("weekEnd") LocalDateTime weekEnd);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :monthStart AND :monthEnd")
    long countNewUsersByMonth(@Param("monthStart") LocalDateTime monthStart,
                              @Param("monthEnd") LocalDateTime monthEnd);

    @Query("SELECT COUNT(DISTINCT ur.user.id) " +
            "FROM UserRoleEntity ur " +
            "WHERE ur.role.id != 3")
    long countUsers();

    @Query("SELECT COUNT(DISTINCT ur.user.id) " +
            "FROM UserRoleEntity ur " +
            "WHERE ur.role.id = 2")
    long countOwners();
}
