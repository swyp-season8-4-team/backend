package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.CouponUseLog;

import java.util.UUID;

@Repository
public interface CouponUseLogRepository extends JpaRepository<CouponUseLog, Long> {
    boolean existsByCouponUuidAndUserUuid(UUID couponUuid, UUID userUuid);
}