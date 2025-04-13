package org.swyp.dessertbee.store.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponCondition.*;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.CouponTypeRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.DiscountCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.GiftCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.CouponStatus;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;
import org.swyp.dessertbee.store.store.entity.Store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;


    public CouponResponse createCoupon(CouponRequest request, Store store) {
        Coupon.CouponBuilder builder = Coupon.builder()
                .name(request.getName())
                .status(CouponStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .store(store)
                .couponTarget(request.getCouponTarget())
                .hasExposureDate(request.getHasExposureDate())
                .exposureStartAt(request.getExposureStartAt())
                .exposureEndAt(request.getExposureEndAt())
                .hasExpiryDate(request.getHasExpiryDate())
                .expiryDate(request.getExpiryDate())
                .hasQuantity(request.getHasQuantity())
                .quantity(request.getQuantity());

        //쿠폰 타입
        CouponTypeRequest detail = request.getCouponDetail();

        builder.type(detail.getType());

        if (detail instanceof DiscountCouponRequest discount) {
            builder.discountType(discount.getDiscountType());
            builder.discountAmount(discount.getDiscountAmount());
        } else if (detail instanceof GiftCouponRequest gift) {
            builder.giftMenuName(gift.getGiftMenuName());
        } else {
            throw new IllegalArgumentException("쿠폰 상세 정보가 올바르지 않습니다.");
        }

        //쿠폰 사용조건 처리
        CouponConditionRequest condition = request.getCouponCondition();

        builder.conditionType(condition.getConditionType());

        if (condition instanceof AmountConditionRequest amount) {
            builder.minimumPurchaseAmount(amount.getMinimumPurchaseAmount());
        } else if (condition instanceof TimeDayConditionRequest timeDay) {
            builder.conditionStartTime(timeDay.getConditionStartTime());
            builder.conditionEndTime(timeDay.getConditionEndTime());
            builder.conditionDays(timeDay.getConditionDays());
        } else if (condition instanceof ExclusiveConditionRequest) {
            builder.exclusiveOnly(false);
        } else if (condition instanceof CustomConditionRequest custom) {
            builder.customConditionText(custom.getCustomConditionText());
        } else {
            throw new IllegalArgumentException("쿠폰 조건 정보가 올바르지 않습니다.");
        }

        Coupon coupon = builder.build();
        couponRepository.save(coupon);

        return CouponResponse.from(coupon);
    }


    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }
}