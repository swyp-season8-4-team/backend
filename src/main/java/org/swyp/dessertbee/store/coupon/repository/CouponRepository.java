package org.swyp.dessertbee.store.coupon.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.UserCoupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByOrderByCreatedAtDesc();

    Optional<Coupon> findByCouponUuid(UUID couponUuid);

    List<Coupon> findAllByHasExpiryDateIsTrueAndStatusIs(CouponStatus status);

}
