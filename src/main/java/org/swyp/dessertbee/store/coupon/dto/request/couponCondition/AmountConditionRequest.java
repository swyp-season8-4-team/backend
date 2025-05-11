package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Schema(description = "최소 금액 조건 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AmountConditionRequest extends BaseCouponConditionRequest {

    @Schema(description = "최소 구매 금액", example = "15000")
    @NotNull
    private Integer minimumPurchaseAmount;

    @Override
    @NotNull
    public CouponConditionType getConditionType() {
        return CouponConditionType.AMOUNT;
    }
}
