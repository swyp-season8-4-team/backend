package org.swyp.dessertbee.store.coupon.dto.request.couponCondition;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(description = "요일 및 시간 조건 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimeDayConditionRequest extends BaseCouponConditionRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "조건 시작 시간", example = "11:00:00",type = "string", format = "time")
    @NotNull
    private LocalTime conditionStartTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "조건 종료 시간", example = "14:00:00",type = "string", format = "time")
    @NotNull
    private LocalTime conditionEndTime;

    @Schema(description = "조건 요일들", example = "[\"MONDAY\", \"WEDNESDAY\"]")
    @NotNull
    private Set<DayOfWeek> conditionDays;

    @Override
    @NotNull
    public CouponConditionType getConditionType() {
        return CouponConditionType.TIME_DAY;
    }
}