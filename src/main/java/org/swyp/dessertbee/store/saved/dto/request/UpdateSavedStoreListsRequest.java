package org.swyp.dessertbee.store.saved.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSavedStoreListsRequest {

    @Schema(
            description = """
            가게를 저장할 리스트들의 목록입니다.
            - 리스트를 하나도 선택하지 않으면, 기존에 저장된 모든 리스트에서 가게가 삭제됩니다.
            - 리스트를 선택하면 해당 리스트에 가게가 저장되며, 선택한 취향 태그(userPreferences)도 함께 저장됩니다.
            """,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<StoreListUpdateRequest> selectedLists;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreListUpdateRequest {

        @Schema(
                description = """
                저장할 리스트의 ID입니다.
                - 필수 값입니다.
                """,
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Long listId;

        @Schema(
                description = """
                사용자가 설정해둔 취향 태그 ID 목록입니다.
                - 선택 값입니다.
                - 설정해둔 취향 태그 ID가 없거나 빈 배열([])이면, 해당 리스트에는 취향 태그 없이 저장됩니다.
                """,
                example = "[1,2,3]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        private List<Long> userPreferences;
    }
}