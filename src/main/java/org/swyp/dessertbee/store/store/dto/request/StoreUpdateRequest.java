package org.swyp.dessertbee.store.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 가게 업데이트 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest extends BaseStoreRequest {

    @Schema(description = "메뉴 정보 목록")
    private List<MenuRequest> menus;

    @Schema(description = "기존에 존재했던 가게 대표 이미지 ID 목록 (없었으면 Null)")
    private List<Long> storeImageDeleteIds;

    @Schema(description = "기존에 존재했던 업주 픽 추가 이미지 ID 목록 (없었으면 Null)")
    private List<Long> ownerPickImageDeleteIds;

    /**
     * 메뉴 요청 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRequest {
        @Schema(description = "메뉴 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
        private UUID menuUuid;

        @Schema(description = "메뉴 이름", example = "수건 케이크")
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        private String name;

        @Schema(description = "메뉴 가격 (원 단위)", example = "5800")
        @NotNull(message = "가격은 필수입니다.")
        private BigDecimal price;

        @Schema(description = "인기 메뉴 여부", example = "true")
        private Boolean isPopular;

        @Schema(description = "메뉴 설명", example = "부드럽고 달콤한 수건 모양 케이크입니다.")
        private String description;

        @Schema(description = "이미지 파일명", example = "image123.png")
        private String imageFileKey;
    }
}