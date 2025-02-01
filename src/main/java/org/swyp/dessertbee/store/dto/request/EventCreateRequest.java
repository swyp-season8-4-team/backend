package org.swyp.dessertbee.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequest {

    @NotBlank
    private String title; // 이벤트 제목

    private String description; // 이벤트 설명 (필수 아님)

    @NotNull
    private LocalDate startDate; // 이벤트 시작일

    @NotNull
    private LocalDate endDate; // 이벤트 종료일
}
