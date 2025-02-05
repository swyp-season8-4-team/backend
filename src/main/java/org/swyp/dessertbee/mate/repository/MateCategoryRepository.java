package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.MateCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateCategoryRepository extends JpaRepository<MateCategory, Long> {

    //주어진 카테고리 ID로 해당 카테고리 이름 조회
    @Query("SELECT DISTINCT mc.name FROM MateCategory mc JOIN Mate m ON mc.mateCategoryId = m.mateCategoryId WHERE mc.mateCategoryId = :mateCategoryId" )
    String findCategoryNameById(@Param("mateCategoryId") Long mateCategoryId);
}

