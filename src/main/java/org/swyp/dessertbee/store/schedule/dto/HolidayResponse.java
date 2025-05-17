package org.swyp.dessertbee.store.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HolidayResponse {
    @NotNull
    @Schema(description = "휴무 시작일 (yyyy.MM.dd)", example = "2025.01.01")
    private String startDate;

    @NotNull
    @Schema(description = "휴무 종료일 (yyyy.MM.dd)", example = "2025.01.03")
    private String endDate;

    @Schema(description = "휴무 사유", example = "신정", nullable = true)
    private String reason;
}