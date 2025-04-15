package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Schema(description = "최소 구매 금액 조건 응답")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountConditionResponse implements CouponConditionResponse {
    @Schema(description = "조건 타입", example = "AMOUNT")
    private CouponConditionType conditionType;

    @Schema(description = "최소 구매 금액", example = "15000")
    private Integer minimumPurchaseAmount;
}
