package org.swyp.dessertbee.store.coupon.dto.request.couponType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftCouponRequest implements CouponTypeRequest {

    @NotBlank
    private String giftMenuName;

    @Override
    public CouponType getType() {
        return CouponType.GIFT;
    }
}
