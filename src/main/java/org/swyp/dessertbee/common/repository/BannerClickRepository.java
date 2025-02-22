package org.swyp.dessertbee.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.BannerClick;

@Repository
public interface BannerClickRepository extends JpaRepository<BannerClick, Long> {
}
