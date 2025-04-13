package org.swyp.dessertbee.user.coupon.service;

import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.user.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.UUID;

public interface UserCouponService {
    IssuedCouponResponse issueCoupon(IssueCouponRequest request, UserEntity user);
    List<IssuedCouponResponse> getUserCoupons(UUID userUuid);
    UserCouponDetailResponse getUserCouponDetail(Long userCouponId, UUID userUuid);
    UsedCouponResponse useCouponByCode(UseCouponRequest request);
    CouponUsageStatusResponse getCouponUsageStats(UUID userUuid);
}
