package org.swyp.dessertbee.store.coupon.dto.response.couponType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCouponResponse implements CouponTypeResponse {
    private CouponType type;
    private String giftMenuName;
}
