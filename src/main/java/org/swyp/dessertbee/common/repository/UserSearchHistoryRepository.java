package org.swyp.dessertbee.common.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.UserSearchHistory;

import java.util.List;

@Repository
public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {

    List<UserSearchHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSearchHistory u WHERE u.userId = :userId AND u.keyword = :keyword")
    void deleteByUserIdAndKeyword(Long userId, String keyword);

    @Query("SELECT h.id FROM UserSearchHistory h WHERE h.userId = :userId ORDER BY h.createdAt DESC")
    List<Long> findRecentSearchIdsByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSearchHistory h WHERE h.userId = :userId AND h.id NOT IN :searchIds")
    void deleteByUserIdAndIdNotIn(Long userId, List<Long> searchIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSearchHistory h WHERE h.userId = :userId AND h.id = :searchId")
    int deleteByUserIdAndId(Long userId, Long searchId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSearchHistory h WHERE h.userId = :userId")
    int deleteByUserId(Long userId);
}