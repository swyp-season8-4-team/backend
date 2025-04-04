package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListResponse {
    @NotNull
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @NotNull
    @Schema(description = "사용자 UUID (비로그인 시 Null)", example = "1c95f3a7-0c7d-4e2b-95cf-ff123abc4567")
    private UUID userUuid;

    @NotBlank
    @Schema(description = "저장 목록 이름", example = "느좋 카페")
    private String listName;

    @NotNull
    @Schema(description = "저장 목록 아이콘 색 ID", example = "1")
    private Long iconColorId;

    @NotNull
    @Schema(description = "저장된 가게 수", example = "30")
    private Integer storeCount;

    @Schema(description = "저장된 가게 정보 리스트", nullable = true)
    private List<SavedStoreResponse> storeData;
}