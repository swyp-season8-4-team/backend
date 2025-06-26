package org.swyp.dessertbee.store.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.schedule.entity.StoreHoliday;

import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static HolidayResponse fromEntity(StoreHoliday holiday) {
        return HolidayResponse.builder()
                .startDate(holiday.getStartDate().format(DATE_FORMATTER))
                .endDate(holiday.getEndDate().format(DATE_FORMATTER))
                .reason(holiday.getReason())
                .build();
    }
}