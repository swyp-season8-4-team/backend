package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.Menu;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 가게의 전체 메뉴 조회
    List<Menu> findByStoreId(Long storeId);
    Optional<Menu> findByIdAndStoreId(Long menuId, Long storeId);
}