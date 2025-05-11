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

/*    @Schema(description = "메뉴 정보 목록")
    private List<MenuRequest> menus;*/

    @Schema(description = "기존에 존재했던 가게 대표 이미지 ID 목록 (없었으면 Null)")
    private List<Long> storeImageDeleteIds;

    @Schema(description = "기존에 존재했던 업주 픽 추가 이미지 ID 목록 (없었으면 Null)")
    private List<Long> ownerPickImageDeleteIds;
}