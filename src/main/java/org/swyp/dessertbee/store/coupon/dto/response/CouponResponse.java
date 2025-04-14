package org.swyp.dessertbee.store.coupon.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.dto.response.couponCondition.CouponConditionResponse;
import org.swyp.dessertbee.store.coupon.dto.response.couponType.CouponTypeResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponTarget;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponse {

    private Long couponId;
    private UUID couponUuid;
    private UUID storeUuid;
    private String name;
    private CouponStatus status; //CREATED, USED, EXPIRED

    private CouponTarget target;

    private Boolean hasExposureDate;
    private LocalDateTime exposureStartAt;
    private LocalDateTime exposureEndAt;

    private Boolean hasExpiryDate;
    private LocalDateTime expiryDate;

    private Boolean hasQuantity;
    private Integer quantity;

    private CouponTypeResponse type;
    private CouponConditionResponse condition;

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
                .type(CouponTypeResponse.of(coupon))
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
                .type(CouponTypeResponse.of(coupon))
                .condition(CouponConditionResponse.of(coupon))
                .build();
    }
}
