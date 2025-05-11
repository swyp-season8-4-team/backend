package org.swyp.dessertbee.store.coupon.dto.response.couponCondition;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "요일/시간 조건 응답")
public class TimeDayConditionResponse implements CouponConditionResponse {
    @Schema(description = "조건 타입", example = "TIME_DAY", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponConditionType conditionType;

    @Schema(description = "조건 시작 시간", example = "09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime conditionStartTime;

    @Schema(description = "조건 종료 시간", example = "18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime conditionEndTime;

    @Schema(description = "조건 요일들", example = "[\"MONDAY\", \"WEDNESDAY\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<DayOfWeek> conditionDays;
}