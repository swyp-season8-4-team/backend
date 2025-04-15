package org.swyp.dessertbee.store.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.dto.request.couponCondition.*;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.CouponTypeRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.DiscountCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.couponType.GiftCouponRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponTarget;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
@Schema(description = "쿠폰 생성 요청")
public class CouponRequest {

    @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
    @NotBlank
    private String name;

    @Schema(description = "노출기간 설정 여부", example = "true")
    @NotNull
    private Boolean hasExposureDate;

    @Schema(description = "노출 시작 시간 (hasExposureDate=true인 경우 필수)", example = "2025-04-15T09:00:00")
    private LocalDateTime exposureStartAt;

    @Schema(description = "노출 종료 시간 (hasExposureDate=true인 경우 필수)", example = "2025-04-16T09:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "사용 조건", oneOf = {
            AmountConditionRequest.class,
            TimeDayConditionRequest.class,
            CustomConditionRequest.class,
            ExclusiveConditionRequest.class
    })
    @NotNull
    private CouponConditionRequest couponCondition;


    @Schema(description = "유효기간 설정 여부", example = "true")
    @NotNull
    private Boolean hasExpiryDate;

    @Schema(description = "유효기간 (hasExpiryDate=true인 경우 필수)", example = "2025-04-30T23:59:59")
    private LocalDateTime expiryDate;

    @NotNull
    @Schema(description = "수량 제한 여부", example = "true")
    private Boolean hasQuantity;

    @Schema(description = "수량 (hasQuantity=true인 경우 필수)", example = "100")
    private Integer quantity;

    @Schema(description = "쿠폰 상세 정보 (할인/증정)", oneOf = {
            DiscountCouponRequest.class,
            GiftCouponRequest.class
    })
    @NotNull
    private CouponTypeRequest couponDetail;

    @Schema(description = "쿠폰 제공 대상 (모든고객:ALL 알림받기한:SUBSRCRIBED 기타: CUSTOM)", example = "ALL")
    @NotNull
    private CouponTarget couponTarget;

    @Schema(description = "가게 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID storeUuid;
}
