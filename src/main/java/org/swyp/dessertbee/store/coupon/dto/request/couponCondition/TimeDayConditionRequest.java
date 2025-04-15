package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.dto.request.BaseCouponConditionRequest;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimeDayConditionRequest extends BaseCouponConditionRequest {

    @NotNull
    private LocalTime conditionStartTime;

    @NotNull
    private LocalTime conditionEndTime;

    @NotNull
    private Set<DayOfWeek> conditionDays;

    @Override
    public CouponConditionType getConditionType() {
        return CouponConditionType.TIME_DAY;
    }
}