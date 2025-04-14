package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListSimpleResponse {
    @NotNull
    @Schema(description = "저장 목록 ID", example = "1234")
    private Long listId;

    @NotBlank
    @Schema(description = "저장 목록 이름", example = "느좋 카페")
    private String listName;

    @NotNull
    @Schema(
            description = """
        저장 목록 아이콘 색 ID
        1 → 노랑 (#FFC803)
        2 → 주황 (#FF8803)
        3 → 초록 (#05D352)
        4 → 파랑 (#00C6D8)
        """,
            example = "1"
    )
    private Long iconColorId;
}
