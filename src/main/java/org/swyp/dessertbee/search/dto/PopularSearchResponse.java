package org.swyp.dessertbee.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularSearchResponse {
    @NotBlank
    @Schema(description = "검색어", example = "케이크")
    private String keyword;

    @NotNull
    @Schema(description = "검색 횟수", example = "123")
    private int searchCount;

    @NotNull
    @Schema(description = "현재 순위", example = "3")
    private int rank;

    @NotNull
    @Schema(description = "이전 순위와의 차이", example = "-2")
    private int difference;
}