package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmountConditionRequest implements CouponConditionRequest {

    @NotNull
    private Integer minimumPurchaseAmount;

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.AMOUNT;
    }
}
