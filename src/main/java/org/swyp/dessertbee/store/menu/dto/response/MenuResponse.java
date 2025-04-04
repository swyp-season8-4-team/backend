package org.swyp.dessertbee.store.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.menu.entity.Menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MenuResponse{
    @Schema(description = "메뉴 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    @NotNull(message = "메뉴 UUID는 필수입니다.")
    private UUID menuUuid;

    @Schema(description = "메뉴 이름", example = "수건 케이크")
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String name;

    @Schema(description = "메뉴 가격 (원 단위)", example = "5800")
    @NotNull(message = "가격은 필수입니다.")
    private BigDecimal price;

    @Schema(description = "인기 메뉴 여부", example = "true", nullable = true)
    private Boolean isPopular;

    @Schema(description = "메뉴 설명", example = "부드럽고 달콤한 수건 모양 케이크입니다.", nullable = true)
    private String description;

    @Schema(description = "메뉴 이미지 파일 URL 리스트", nullable = true)
    private List<String> images;

    public static MenuResponse fromEntity(Menu menu, List<String> images) {
        return new MenuResponse(
                menu.getMenuUuid(),
                menu.getName(),
                menu.getPrice(),
                menu.getIsPopular(),
                menu.getDescription(),
                images
        );
    }
}
