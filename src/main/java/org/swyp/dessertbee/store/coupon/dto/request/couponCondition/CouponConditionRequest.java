package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "conditionType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmountConditionRequest.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = TimeDayConditionRequest.class, name = "TIME_DAY"),
        @JsonSubTypes.Type(value = ExclusiveConditionRequest.class, name = "EXCLUSIVE"),
        @JsonSubTypes.Type(value = CustomConditionRequest.class, name = "CUSTOM")
})
public interface CouponConditionRequest {
    CouponConditionType getConditionType();
}
