package org.swyp.dessertbee.store.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.dto.request.couponCondition.CouponConditionRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.CouponTypeRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponTarget;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
public class CouponRequest {
    @NotBlank
    private String name;

    @NotNull
    private Boolean hasExposureDate;
    private LocalDateTime exposureStartAt;
    private LocalDateTime exposureEndAt;

    @NotNull
    private CouponConditionRequest couponCondition;

    @NotNull
    private Boolean hasExpiryDate;
    private LocalDateTime expiryDate;

    @NotNull
    private Boolean hasQuantity;
    private Integer quantity;

    @NotNull
    private CouponTypeRequest couponDetail;

    @NotNull
    private CouponTarget couponTarget;

    private UUID storeUuid;
}
