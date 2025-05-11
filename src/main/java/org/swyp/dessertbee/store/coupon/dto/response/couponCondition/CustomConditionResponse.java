package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "커스텀 사용 조건 응답")
public class CustomConditionResponse implements CouponConditionResponse {
    @Schema(description = "조건 타입", example = "CUSTOM", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponConditionType conditionType;

    @Schema(description = "커스텀 조건 텍스트", example = "해피아워에만 사용 가능", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customConditionText;
}