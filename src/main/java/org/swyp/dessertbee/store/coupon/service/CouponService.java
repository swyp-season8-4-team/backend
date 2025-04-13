package org.swyp.dessertbee.store.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponCondition.*;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.CouponTypeRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.DiscountCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.GiftCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
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

    /**
     * 쿠폰 생성
     */
    public CouponResponse createCoupon(CouponRequest request, Store store) {
        validateCouponRequest(request);

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
        }

        Coupon coupon = builder.build();
        couponRepository.save(coupon);

        return CouponResponse.from(coupon);
    }

    /**
     * 쿠폰 수정
     */
    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponRequest request, Store store) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getStore().getStoreId().equals(store.getStoreId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        coupon.updateBasicInfo(
                request.getName(),
                request.getCouponTarget(),
                request.getHasExposureDate(),
                request.getExposureStartAt(),
                request.getExposureEndAt(),
                request.getHasExpiryDate(),
                request.getExpiryDate(),
                request.getHasQuantity(),
                request.getQuantity()
        );

        // 쿠폰 타입에 따른 필드 수정
        CouponTypeRequest detail = request.getCouponDetail();
        coupon.updateType(detail);

        // 쿠폰 조건 수정
        CouponConditionRequest condition = request.getCouponCondition();
        coupon.updateCondition(condition);

        return CouponResponse.from(coupon);
    }

    /**
     * 쿠폰 삭제
     */
    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        //소속 가게 체크
        Store store = coupon.getStore();
        if (!coupon.getStore().getStoreId().equals(store.getStoreId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        couponRepository.delete(coupon);
    }

    /**
     * 생성한 쿠폰 조회
     */
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    //----------------------validate---------------

    /**
     * 쿠폰 요청 유효성 검증
     */
    private void validateCouponRequest(CouponRequest request) {
        // 이름 필수
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_COUPON_NAME);
        }

        // 수량 제한 여부
        if (Boolean.FALSE.equals(request.getHasQuantity()) && request.getQuantity() != null) {
            throw new BusinessException(ErrorCode.QUANTITY_NOT_ALLOWED);
        }

        // 유효기간 설정 여부
        if (Boolean.FALSE.equals(request.getHasExpiryDate()) && request.getExpiryDate() != null) {
            throw new BusinessException(ErrorCode.EXPIRY_DATE_NOT_ALLOWED);
        }

        // 노출기간 설정 여부
        if (Boolean.FALSE.equals(request.getHasExposureDate()) && (request.getExposureStartAt() != null || request.getExposureEndAt() != null)) {
            throw new BusinessException(ErrorCode.EXPOSURE_DATE_NOT_ALLOWED);
        }

        // 쿠폰 상세 정보
        CouponTypeRequest detail = request.getCouponDetail();
        if (detail == null || detail.getType() == null) {
            throw new BusinessException(ErrorCode.INVALID_COUPON_DETAIL);
        }

        if (detail.getType() == CouponType.DISCOUNT) {
            if (!(detail instanceof DiscountCouponRequest discountDetail)) {
                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_DETAIL);
            }
            if (discountDetail.getDiscountType() == null || discountDetail.getDiscountAmount() == null) {
                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_DETAIL);
            }
            // 할인 쿠폰일 경우 증정 필드에 값이 있으면 안됨
            if (detail instanceof GiftCouponRequest) {
                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_FOR_GIFT);
            }
        }

        if (detail.getType() == CouponType.GIFT) {
            if (!(detail instanceof GiftCouponRequest giftDetail)) {
                throw new BusinessException(ErrorCode.INVALID_GIFT_DETAIL);
            }
            if (giftDetail.getGiftMenuName() == null || giftDetail.getGiftMenuName().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_GIFT_DETAIL);
            }
            // 증정 쿠폰일 경우 할인 관련 필드가 채워져 있으면 안됨
            if (detail instanceof DiscountCouponRequest) {
                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_FOR_GIFT);
            }
        }

        // 사용조건에 따른 필드 유효성 검사
        CouponConditionRequest condition = request.getCouponCondition();
        CouponConditionType conditionType = condition.getConditionType();

        switch (conditionType) {
            case AMOUNT:
                validateAmountCondition((AmountConditionRequest) condition);
                break;
            case TIME_DAY:
                validateTimeDayCondition((TimeDayConditionRequest) condition);
                break;
            case CUSTOM:
                validateCustomCondition((CustomConditionRequest) condition);
                break;
            case EXCLUSIVE:
                validateExclusiveCondition((ExclusiveConditionRequest) condition);
                break;
            default:
                throw new BusinessException(ErrorCode.INVALID_CONDITION_TYPE);
        }
    }
    private void validateAmountCondition(AmountConditionRequest condition) {
        if (condition.getMinimumPurchaseAmount() == null) {
            throw new BusinessException(ErrorCode.MIN_PURCHASE_AMOUNT_REQUIRED);
        }
        // 그 외 필드는 null 또는 비어 있어야 함
        if (condition.getConditionStartTime() != null || condition.getConditionEndTime() != null ||
                condition.getConditionDays() != null || condition.getCustomConditionText() != null ||
                condition.getExclusiveOnly() != null) {
            throw new BusinessException(ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_AMOUNT);
        }
    }

    private void validateTimeDayCondition(TimeDayConditionRequest condition) {
        if (condition.getConditionStartTime() == null || condition.getConditionEndTime() == null || condition.getConditionDays() == null) {
            throw new BusinessException(ErrorCode.TIME_DAY_FIELDS_REQUIRED);
        }
        // 그 외 필드는 null 또는 비어 있어야 함
        if (condition.getMinimumPurchaseAmount() != null || condition.getCustomConditionText() != null ||
                condition.getExclusiveOnly() != null) {
            throw new BusinessException(ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_TIME_DAY);
        }
    }

    private void validateCustomCondition(CustomConditionRequest condition) {
        if (condition.getCustomConditionText() == null) {
            throw new BusinessException(ErrorCode.CUSTOM_CONDITION_TEXT_REQUIRED);
        }
        // 그 외 필드는 null 또는 비어 있어야 함
        if (condition.getMinimumPurchaseAmount() != null || condition.getConditionStartTime() != null ||
                condition.getConditionEndTime() != null || condition.getConditionDays() != null ||
                condition.getExclusiveOnly() != null) {
            throw new BusinessException(ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_CUSTOM);
        }
    }

    private void validateExclusiveCondition(ExclusiveConditionRequest condition) {
        if (condition.getExclusiveOnly() == null || condition.getExclusiveOnly()) {
            throw new BusinessException(ErrorCode.EXCLUSIVE_FIELD_REQUIRED);
        }
        // 그 외 필드는 null 또는 비어 있어야 함
        if (condition.getMinimumPurchaseAmount() != null || condition.getConditionStartTime() != null ||
                condition.getConditionEndTime() != null || condition.getConditionDays() != null ||
                condition.getCustomConditionText() != null) {
            throw new BusinessException(ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_EXCLUSIVE);
        }
    }
}