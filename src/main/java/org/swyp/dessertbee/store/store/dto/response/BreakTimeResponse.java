package org.swyp.dessertbee.store.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class BreakTimeResponse {

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "휴게 시작 시간", example = "14:00")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "휴게 종료 시간", example = "15:00")
    private LocalTime endTime;
}