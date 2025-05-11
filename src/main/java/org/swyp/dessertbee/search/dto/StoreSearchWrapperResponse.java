package org.swyp.dessertbee.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.store.dto.response.StoreSummaryResponse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "검색 결과 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreSearchWrapperResponse {

    @Schema(description = "가게 검색 결과 목록 (검색 결과가 여러 개일 경우에만 사용)")
    private List<StoreSearchResponse> stores;

    @Schema(description = "검색 결과가 하나일 경우 자동으로 포함되는 간략 정보")
    private StoreSummaryResponse summary;
}
