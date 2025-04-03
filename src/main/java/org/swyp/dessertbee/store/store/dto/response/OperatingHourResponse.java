package org.swyp.dessertbee.store.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OperatingHourResponse {

    @Schema(description = "요일", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "오픈 시간", example = "10:00")
    private LocalTime openingTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "마감 시간", example = "20:00")
    private LocalTime closingTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "라스트 오더 시간", example = "19:30")
    private LocalTime lastOrderTime;

    @Schema(description = "해당 요일 휴무 여부", example = "false")
    private Boolean isClosed;

    @Schema(description = "정기 휴무 유형", example = "MONTHLY")
    private String regularClosureType;

    @Schema(description = "정기 휴무 주차", example = "1,3")
    private String regularClosureWeeks;

    @Schema(description = "휴게 시간 목록")
    private List<BreakTimeResponse> breakTimes;
}