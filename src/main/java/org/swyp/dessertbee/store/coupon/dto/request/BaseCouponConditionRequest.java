package org.swyp.dessertbee.store.coupon.dto.request;

import lombok.Getter;
import org.swyp.dessertbee.store.coupon.dto.request.couponCondition.CouponConditionRequest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
public abstract class BaseCouponConditionRequest implements CouponConditionRequest {

    // 모든 조건 타입에서 공통적으로 검증할 수 있도록 전부 선언
    private Integer minimumPurchaseAmount;
    private LocalTime conditionStartTime;
    private LocalTime conditionEndTime;
    private Set<DayOfWeek> conditionDays;
    private String customConditionText;
    private Boolean exclusiveOnly;

}
