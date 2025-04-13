package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomConditionRequest implements CouponConditionRequest {

    @NotBlank
    private String customConditionText;

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.CUSTOM;
    }
}
