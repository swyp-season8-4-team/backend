package org.swyp.dessertbee.store.coupon.service;

import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.user.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.UUID;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request, Store store);
    CouponResponse updateCoupon(Long couponId, CouponRequest request, Store store);
    void deleteCoupon(Long couponId);
    List<CouponResponse> getAllCoupons();
}
