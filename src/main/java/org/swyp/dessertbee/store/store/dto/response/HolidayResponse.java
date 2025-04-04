package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HolidayResponse {
    @NotBlank
    @Schema(description = "휴무 일자", example = "2025-01-01")
    private String date;

    @Schema(description = "휴무 사유", example = "신정", nullable = true)
    private String reason;
}