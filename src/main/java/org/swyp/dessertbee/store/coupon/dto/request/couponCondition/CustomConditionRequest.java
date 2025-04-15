package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Schema(description = "커스텀 조건 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomConditionRequest extends BaseCouponConditionRequest {

    @Schema(description = "커스텀 조건 설명", example = "선착순 50명 한정")
    @NotBlank
    private String customConditionText;

    @Override
    @NotNull
    public CouponConditionType getConditionType() {
        return CouponConditionType.CUSTOM;
    }
}
