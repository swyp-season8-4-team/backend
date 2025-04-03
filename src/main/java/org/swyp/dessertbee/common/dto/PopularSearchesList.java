package org.swyp.dessertbee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "실시간 인기 검색어 응답")
public class PopularSearchesList {

    @Schema(description = "검색어 데이터 최종 업데이트 시간 (ISO 8601)", example = "2025-04-04T10:00:00Z")
    private String lastUpdatedTime;

    @Schema(description = "인기 검색어 리스트")
    private List<PopularSearchResponse> searches;
}
