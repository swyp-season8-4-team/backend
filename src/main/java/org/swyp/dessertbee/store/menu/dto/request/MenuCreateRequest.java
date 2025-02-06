package org.swyp.dessertbee.store.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {

    @NotBlank
    private String name; // 메뉴 이름

    @NotNull
    private BigDecimal price; // 가격

    private Boolean isPopular = false; // 인기 메뉴 여부 (기본값: false)

    private String description; // 메뉴 설명
}
