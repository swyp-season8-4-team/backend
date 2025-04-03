package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StoreListLocationResponse {
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @Schema(description = "저장 목록 아이콘 색 ID", example = "1")
    private Long iconColorId;

    @Schema(description = "가게 ID", example = "12")
    private Long storeId;

    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private BigDecimal latitude;

    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private BigDecimal longitude;
}
