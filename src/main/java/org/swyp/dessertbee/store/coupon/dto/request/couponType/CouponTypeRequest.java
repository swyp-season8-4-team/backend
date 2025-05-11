package org.swyp.dessertbee.store.coupon.dto.request.couponType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiscountCouponRequest.class, name = "DISCOUNT"),
        @JsonSubTypes.Type(value = GiftCouponRequest.class, name = "GIFT")
})
public interface CouponTypeRequest {
    CouponType getType();
}
