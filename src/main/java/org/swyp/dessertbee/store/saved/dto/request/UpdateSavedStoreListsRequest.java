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

    @Schema(description = "가게를 저장할 리스트 목록")
    private List<StoreListUpdateRequest> selectedLists;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreListUpdateRequest {

        @Schema(description = "리스트 ID", example = "1")
        private Long listId;

        @Schema(description = "사용자가 선택한 취향 태그 ID 목록", example = "[101, 102, 103]")
        private List<Long> userPreferences;
    }
}