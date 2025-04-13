package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@AllArgsConstructor
@Builder
public class ExclusiveConditionRequest extends BaseCouponConditionRequest {

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.EXCLUSIVE;
    }
}
