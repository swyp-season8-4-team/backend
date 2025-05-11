package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "단독 사용 불가 조건(false) 응답")
public class ExclusiveConditionResponse implements CouponConditionResponse {
    @Schema(description = "조건 타입", example = "EXCLUSIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponConditionType conditionType;
}
