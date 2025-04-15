package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import jakarta.validation.constraints.NotBlank;
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
public class CustomConditionRequest extends BaseCouponConditionRequest {

    @NotBlank
    private String customConditionText;

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.CUSTOM;
    }
}
