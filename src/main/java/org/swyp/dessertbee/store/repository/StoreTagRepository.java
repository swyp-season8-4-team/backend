package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.StoreTag;
import org.swyp.dessertbee.store.entity.TagCategory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StoreTagRepository extends JpaRepository<StoreTag, Long> {

    // 태그 이름으로 조회
    Optional<StoreTag> findByName(String name);

    // 주어진 ID 목록에 해당하는 태그 조회 (1~3개 선택 시 사용)
    List<StoreTag> findByIdIn(List<Long> ids);
}