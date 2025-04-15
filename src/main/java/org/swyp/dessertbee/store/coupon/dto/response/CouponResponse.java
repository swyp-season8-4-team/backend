package org.swyp.dessertbee.store.coupon.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.dto.response.couponCondition.*;
import org.swyp.dessertbee.store.coupon.dto.response.couponType.CouponTypeResponse;
import org.swyp.dessertbee.store.coupon.dto.response.couponType.DiscountCouponResponse;
import org.swyp.dessertbee.store.coupon.dto.response.couponType.GiftCouponResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponTarget;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "쿠폰 응답")
@Data
@Builder
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponse {


    @Schema(description = "쿠폰 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long couponId;

    @Schema(description = "쿠폰 UUID", example = "a123e456-78b9-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID couponUuid;

    @Schema(description = "가게 UUID", example = "d1b44bb2-50f5-4823-a017-1a2ff3285b1a", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID storeUuid;

    @Schema(description = "쿠폰 이름", example = "봄맞이 10% 할인 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "쿠폰 상태(CREATED, EXPIRED)", example = "CREATED", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponStatus status;

    @Schema(description = "쿠폰 제공 대상", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponTarget target;

    @Schema(description = "노출 설정 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasExposureDate;

    @Schema(description = "노출 시작일", example = "2025-05-01T00:00:00")
    private LocalDateTime exposureStartAt;

    @Schema(description = "노출 종료일", example = "2025-05-10T23:59:59")
    private LocalDateTime exposureEndAt;

    @Schema(description = "유효기간 설정 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasExpiryDate;

    @Schema(description = "만료일", example = "2025-06-01T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "수량 제한 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasQuantity;

    @Schema(description = "수량", example = "100")
    private Integer quantity;

    @Schema(description = "쿠폰 타입", oneOf = {DiscountCouponResponse.class, GiftCouponResponse.class}, requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponTypeResponse couponType;

    @Schema(description = "쿠폰 사용 조건", oneOf = {
            AmountConditionResponse.class,
            CustomConditionResponse.class,
            ExclusiveConditionResponse.class,
            TimeDayConditionResponse.class
    }, requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponConditionResponse condition;

    @Schema(description = "생성일", example = "2025-04-15T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getId())
                .couponUuid(coupon.getCouponUuid())
                .storeUuid(coupon.getStore().getStoreUuid())
                .name(coupon.getName())
                .status(coupon.getStatus())
                .target(coupon.getCouponTarget())
                .hasExposureDate(coupon.getHasExposureDate())
                .exposureStartAt(coupon.getExposureStartAt())
                .exposureEndAt(coupon.getExposureEndAt())
                .hasExpiryDate(coupon.getHasExpiryDate())
                .expiryDate(coupon.getExpiryDate())
                .hasQuantity(coupon.getHasQuantity())
                .quantity(coupon.getQuantity())
                .couponType(CouponTypeResponse.of(coupon))
                .condition(CouponConditionResponse.of(coupon))
                .createdAt(coupon.getCreatedAt())
                .build();
    }

    public static CouponResponse list(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getId())
                .couponUuid(coupon.getCouponUuid())
                .storeUuid(coupon.getStore().getStoreUuid())
                .name(coupon.getName())
                .status(coupon.getStatus())
                .target(coupon.getCouponTarget())
                .hasExposureDate(coupon.getHasExposureDate())
                .exposureStartAt(coupon.getExposureStartAt())
                .exposureEndAt(coupon.getExposureEndAt())
                .hasQuantity(coupon.getHasQuantity())
                .quantity(coupon.getQuantity())
                .couponType(CouponTypeResponse.of(coupon))
                .condition(CouponConditionResponse.of(coupon))
                .build();
    }
}
