package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListResponse {
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @Schema(description = "사용자 UUID (비로그인 시 Null)", example = "1c95f3a7-0c7d-4e2b-95cf-ff123abc4567")
    private UUID userUuid;

    @Schema(description = "저장 목록 이름", example = "느좋 카페")
    private String listName;

    @Schema(description = "저장 목록 아이콘 색 ID", example = "1")
    private Long iconColorId;

    @Schema(description = "저장된 가게 수", example = "30")
    private int storeCount;

    @Schema(description = "저장된 가게 정보 리스트")
    private List<SavedStoreResponse> storeData;
}