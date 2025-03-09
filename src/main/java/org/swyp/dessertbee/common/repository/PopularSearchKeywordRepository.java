package org.swyp.dessertbee.common.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.PopularSearchKeyword;

import java.util.Optional;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {

    Optional<PopularSearchKeyword> findByKeyword(String keyword);

    @Modifying
    @Transactional
    @Query("UPDATE PopularSearchKeyword p SET p.searchCount = p.searchCount + :count WHERE p.keyword = :keyword")
    void incrementSearchCount(@Param("keyword") String keyword, @Param("count") int count);
}