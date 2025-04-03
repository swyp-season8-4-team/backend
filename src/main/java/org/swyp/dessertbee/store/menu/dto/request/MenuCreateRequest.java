package org.swyp.dessertbee.store.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 메뉴 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {
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