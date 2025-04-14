package org.swyp.dessertbee.store.coupon.repository;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByOrderByCreatedAtDesc();

    Optional<Coupon> findByCouponUuid(UUID couponUuid);

    List<Coupon> findAllByHasExpiryDateIsTrueAndStatusIs(CouponStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.couponUuid = :couponUuid")
    Optional<Coupon> findByCouponUuidForUpdate(@Param("couponUuid") UUID couponUuid);

}
