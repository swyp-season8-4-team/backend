package org.swyp.dessertbee.store.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 가게 생성 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest extends BaseStoreRequest {

    @Builder.Default
    @Schema(description = "가게 상태 (PENDING, APPROVED, REJECTED), 기본값 설정되어 있으므로 프론트에서 따로 보낼 필요 없음")
    private StoreStatus status = StoreStatus.APPROVED;

    @Schema(description = "등록할 메뉴 정보 목록")
    private List<MenuCreateRequest> menus;

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

    // StoreLinkRequest 클래스 재정의 없이 상속받아 사용
}