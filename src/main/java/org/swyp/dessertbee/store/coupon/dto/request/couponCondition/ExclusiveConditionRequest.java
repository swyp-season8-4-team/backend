package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

@Schema(description = "단독 사용 불가 조건(false) 요청")
@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ExclusiveConditionRequest extends BaseCouponConditionRequest {

    @Override
    @NotNull
    public CouponConditionType getConditionType() {
        return CouponConditionType.EXCLUSIVE;
    }
}
