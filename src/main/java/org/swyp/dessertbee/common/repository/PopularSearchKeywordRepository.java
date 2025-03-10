package org.swyp.dessertbee.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.PopularSearchKeyword;

import java.util.Optional;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {
    Optional<PopularSearchKeyword> findByKeyword(String keyword);
}