package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.mate.entity.MateCategory;

import java.util.Optional;

public interface MateCategoryRepository extends JpaRepository<MateCategory, Long> {

    //주어진 카테고리 ID로 해당 카테고리 이름 조회
    Optional<MateCategory> findById(Long mateCategoryid);
}
