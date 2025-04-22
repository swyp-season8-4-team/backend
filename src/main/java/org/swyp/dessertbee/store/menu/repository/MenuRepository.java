package org.swyp.dessertbee.store.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.menu.entity.Menu;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 특정 가게의 전체 메뉴 조회
    List<Menu> findByStoreIdAndDeletedAtIsNull(Long storeId);
    Optional<Menu> findByMenuIdAndStoreIdAndDeletedAtIsNull(Long menuId, Long storeId);

    @Query("SELECT m.menuId FROM Menu m WHERE m.menuUuid = :menuUuid")
    Long findMenuIdByMenuUuid(@Param("menuUuid") UUID menuUuid);

    @Query("SELECT m.name FROM Menu m WHERE m.storeId = :storeId AND m.deletedAt IS NULL")
    List<String> findMenuNamesByStoreId(@Param("storeId") Long storeId);

}