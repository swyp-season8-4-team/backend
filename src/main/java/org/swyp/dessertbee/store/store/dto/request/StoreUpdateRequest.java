package org.swyp.dessertbee.store.store.dto.request;

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

    private List<MenuRequest> menus;
    private List<Long> storeImageDeleteIds;
    private List<Long> ownerPickImageDeleteIds;

    /**
     * 메뉴 요청 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRequest {
        private UUID menuUuid;
        private String name;
        private BigDecimal price;
        private Boolean isPopular;
        private String description;
        private String imageFileKey;
    }
}