package org.swyp.dessertbee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularSearchResponse {
    @Schema(description = "검색어", example = "케이크")
    private String keyword;

    @Schema(description = "검색 횟수", example = "123")
    private int searchCount;

    @Schema(description = "현재 순위", example = "3")
    private int rank;

    @Schema(description = "이전 순위와의 차이", example = "-2")
    private int difference;
}