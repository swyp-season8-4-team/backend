package org.swyp.dessertbee.user.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.user.coupon.entity.UserCoupon;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List <UserCoupon> findAllByUser_UserUuid(UUID userUuid);

    Optional<UserCoupon> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String code);

    boolean existsByUserAndCoupon(UserEntity user, Coupon coupon);
}
