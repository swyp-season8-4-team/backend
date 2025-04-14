package org.swyp.dessertbee.store.coupon.dto.response.couponType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
import org.swyp.dessertbee.store.coupon.entity.enums.DiscountType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCouponResponse implements CouponTypeResponse {
    private CouponType type;
    private DiscountType discountType;
    private Integer discountAmount;
}

