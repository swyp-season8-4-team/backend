package org.swyp.dessertbee.store.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.schedule.entity.StoreOperatingHour;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OperatingHourResponse {

    @NotNull
    @Schema(description = "요일", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "오픈 시간", example = "10:00")
    private LocalTime openingTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "마감 시간", example = "20:00")
    private LocalTime closingTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "라스트 오더 시간", example = "19:30", nullable = true)
    private LocalTime lastOrderTime;

    @NotNull
    @Schema(description = "해당 요일 휴무 여부", example = "false")
    private Boolean isClosed;

    @Schema(description = "정기 휴무 유형", example = "MONTHLY", nullable = true)
    private String regularClosureType;

    @Schema(description = "정기 휴무 주차", example = "1,3", nullable = true)
    private String regularClosureWeeks;

    @Schema(description = "휴게 시간 목록", nullable = true)
    private List<BreakTimeResponse> breakTimes;

    public static OperatingHourResponse fromEntity(StoreOperatingHour operatingHour) {
        return OperatingHourResponse.builder()
                .dayOfWeek(operatingHour.getDayOfWeek())
                .openingTime(operatingHour.getOpeningTime())
                .closingTime(operatingHour.getClosingTime())
                .lastOrderTime(operatingHour.getLastOrderTime())
                .isClosed(operatingHour.getIsClosed())
                .regularClosureType(operatingHour.getRegularClosureType() != null ?
                        operatingHour.getRegularClosureType().name() : null)
                .regularClosureWeeks(operatingHour.getRegularClosureWeeks())
                .breakTimes(Collections.emptyList())
                .build();
    }
}