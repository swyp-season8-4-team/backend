package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StoreListLocationResponse {
    @NotNull
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @NotNull
    @Schema(description = "저장 목록 아이콘 색 ID", example = "1")
    private Long iconColorId;

    @NotNull
    @Schema(description = "가게 ID", example = "12")
    private Long storeId;

    @NotBlank
    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @NotNull
    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private BigDecimal longitude;
}
