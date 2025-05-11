package org.swyp.dessertbee.store.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.dessertbee.store.tag.entity.StoreTag;
import org.swyp.dessertbee.store.tag.entity.TagCategory;

@Getter
@Builder
@AllArgsConstructor
public class StoreTagResponse {

    @Schema(description = "태그 ID", example = "11")
    private Long id;

    @Schema(description = "태그명", example = "케이크")
    private String name;

    @Schema(description = "카테고리 정보")
    private TagCategoryResponse category;

    public static StoreTagResponse fromEntity(StoreTag tag) {
        return StoreTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(TagCategoryResponse.fromEntity(tag.getCategory()))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TagCategoryResponse {

        @Schema(description = "카테고리 ID", example = "2")
        private Long id;

        @Schema(description = "카테고리명", example = "디저트")
        private String name;

        public static TagCategoryResponse fromEntity(TagCategory category) {
            return TagCategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        }
    }
}
