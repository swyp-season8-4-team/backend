package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class SavedStoreResponse {
    @Schema(description = "사용자 UUID (비로그인 시 Null)", example = "1c95f3a7-0c7d-4e2b-95cf-ff123abc4567")
    private UUID userUuid;

    @Schema(description = "가게 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    private UUID storeUuid;

    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @Schema(description = "저장 목록 이름", example = "느좋 카페")
    private String listName;

    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String storeName;

    @Schema(description = "가게 주소", example = "서울 마포구 양화로 23길 8")
    private String storeAddress;

    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private BigDecimal latitude;

    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private BigDecimal longitude;

    @Schema(description = "매장 대표 이미지 URL 리스트")
    private List<String> imageUrls;

    @Schema(description = "사용자의 취향 ID 리스트")
    private List<Long> userPreferences;
}
