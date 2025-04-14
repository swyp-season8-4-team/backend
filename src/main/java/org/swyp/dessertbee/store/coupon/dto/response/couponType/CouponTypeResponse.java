package org.swyp.dessertbee.store.coupon.dto.response.couponType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

import static org.swyp.dessertbee.store.coupon.entity.enums.CouponType.DISCOUNT;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiscountCouponResponse.class, name = "DISCOUNT"),
        @JsonSubTypes.Type(value = GiftCouponResponse.class, name = "GIFT")
})
public interface CouponTypeResponse {
    CouponType getType();

    static CouponTypeResponse of(Coupon coupon) {
        return switch (coupon.getType()) {
            case DISCOUNT -> new DiscountCouponResponse(
                    DISCOUNT,
                    coupon.getDiscountType(),
                    coupon.getDiscountAmount()
            );
            case GIFT -> new GiftCouponResponse(
                    CouponType.GIFT,
                    coupon.getGiftMenuName()
            );
        };
    }
}
