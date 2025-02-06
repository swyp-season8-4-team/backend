package org.swyp.dessertbee.store.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.menu.entity.Menu;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 가게의 전체 메뉴 조회
    List<Menu> findByStoreIdAndDeletedAtIsNull(Long storeId);
    Optional<Menu> findByIdAndStoreIdAndDeletedAtIsNull(Long menuId, Long storeId);
    boolean existsByStoreIdAndNameAndDeletedAtIsNull(Long storeId, String name);
}