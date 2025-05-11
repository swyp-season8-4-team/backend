package org.swyp.dessertbee.store.coupon.dto.response.couponType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
import org.swyp.dessertbee.store.coupon.entity.enums.DiscountType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "할인 쿠폰 응답")
public class DiscountCouponResponse implements CouponTypeResponse {
    @Schema(description = "쿠폰 타입", example = "DISCOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponType type;

    @Schema(description = "할인 타입", example = "FIXED", requiredMode = Schema.RequiredMode.REQUIRED)
    private DiscountType discountType;

    @Schema(description = "할인 금액/비율", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer discountAmount;
}