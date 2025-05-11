package org.swyp.dessertbee.store.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class BreakTimeResponse {

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "휴게 시작 시간", example = "14:00")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "휴게 종료 시간", example = "15:00")
    private LocalTime endTime;
}