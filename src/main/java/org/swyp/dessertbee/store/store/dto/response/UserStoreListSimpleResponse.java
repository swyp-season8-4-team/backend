package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListSimpleResponse {
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @Schema(description = "저장 목록 이름", example = "느좋 카페")
    private String listName;

    @Schema(description = "저장 목록 아이콘 색 ID", example = "1")
    private Long iconColorId;
}
