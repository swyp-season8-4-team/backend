package org.swyp.dessertbee.store.menu.dto.request;

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
    private String name;
    private BigDecimal price;
    private Boolean isPopular;
    private String description;
    private String imageFileKey;
}