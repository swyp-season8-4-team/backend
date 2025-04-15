package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AmountConditionRequest extends BaseCouponConditionRequest {

    @NotNull
    private Integer minimumPurchaseAmount;

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.AMOUNT;
    }
}
