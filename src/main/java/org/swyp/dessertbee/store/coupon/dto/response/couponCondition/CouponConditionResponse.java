package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "conditionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmountConditionResponse.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = TimeDayConditionResponse.class, name = "TIME_DAY"),
        @JsonSubTypes.Type(value = ExclusiveConditionResponse.class, name = "EXCLUSIVE"),
        @JsonSubTypes.Type(value = CustomConditionResponse.class, name = "CUSTOM")
})
public interface CouponConditionResponse {
    CouponConditionType getConditionType();

    static CouponConditionResponse of(Coupon coupon) {
        if (coupon.getMinimumPurchaseAmount() != null) {
            return new AmountConditionResponse(CouponConditionType.AMOUNT, coupon.getMinimumPurchaseAmount());
        } else if (coupon.getConditionStartTime() != null) {
            return new TimeDayConditionResponse(
                    CouponConditionType.TIME_DAY,
                    coupon.getConditionStartTime(),
                    coupon.getConditionEndTime(),
                    coupon.getConditionDays()
            );
        } else if (Boolean.FALSE.equals(coupon.getExclusiveOnly())) {
            return new ExclusiveConditionResponse(CouponConditionType.EXCLUSIVE);
        } else if (coupon.getCustomConditionText() != null) {
            return new CustomConditionResponse(CouponConditionType.CUSTOM, coupon.getCustomConditionText());
        } else {
            throw new IllegalStateException("쿠폰 조건이 명확하지 않습니다.");
        }
    }
}

