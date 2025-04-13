package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExclusiveConditionResponse implements CouponConditionResponse {
    private CouponConditionType conditionType;
}
