package org.swyp.dessertbee.store.coupon.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.swyp.dessertbee.store.coupon.entity.UserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends CrudRepository<UserCoupon, Long> {
    List <UserCoupon> findAllByUserId(Long userId);
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.code = :code")
    Optional<UserCoupon> findByCode(String code);
}
