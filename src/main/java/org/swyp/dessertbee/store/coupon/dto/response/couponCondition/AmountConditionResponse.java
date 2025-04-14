package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountConditionResponse implements CouponConditionResponse {
    private CouponConditionType conditionType;
    private Integer minimumPurchaseAmount;
}
