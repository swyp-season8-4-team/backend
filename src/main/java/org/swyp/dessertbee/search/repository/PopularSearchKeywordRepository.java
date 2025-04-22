package org.swyp.dessertbee.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.search.entity.PopularSearchKeyword;

import java.util.Optional;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {
    Optional<PopularSearchKeyword> findByKeyword(String keyword);
}