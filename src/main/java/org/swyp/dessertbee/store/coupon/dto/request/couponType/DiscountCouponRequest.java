package org.swyp.dessertbee.store.coupon.dto.request.couponType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
import org.swyp.dessertbee.store.coupon.entity.enums.DiscountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCouponRequest implements CouponTypeRequest {

    @NotNull
    private DiscountType discountType;

    @NotNull
    private Integer discountAmount;

    @Override
    public CouponType getType() {
        return CouponType.DISCOUNT;
    }
}
