package org.swyp.dessertbee.store.coupon.dto.request.couponType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
import org.swyp.dessertbee.store.coupon.entity.enums.DiscountType;

@Schema(description = "할인 쿠폰 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCouponRequest implements CouponTypeRequest {

    @Schema(description = "할인 타입 (정액: FIXED, 정률: RATE)", example = "FIXED")
    @NotNull
    private DiscountType discountType;

    @Schema(description = "할인 금액 또는 할인율", example = "30")
    @NotNull
    private Integer discountAmount;

    @Override
    public CouponType getType() {
        return CouponType.DISCOUNT;
    }
}
