package org.swyp.dessertbee.common.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.PopularSearchKeyword;

import java.util.List;
import java.util.Optional;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {

    @Query("SELECT p FROM PopularSearchKeyword p ORDER BY p.searchCount DESC LIMIT :limit")
    List<PopularSearchKeyword> findTopKeywords(@Param("limit") int limit);

    Optional<PopularSearchKeyword> findByKeyword(String keyword);
}