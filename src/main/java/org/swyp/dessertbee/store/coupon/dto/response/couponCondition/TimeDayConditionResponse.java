package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeDayConditionResponse implements CouponConditionResponse {
    private CouponConditionType conditionType;
    private LocalTime conditionStartTime;
    private LocalTime conditionEndTime;
    private Set<DayOfWeek> conditionDays;
}
