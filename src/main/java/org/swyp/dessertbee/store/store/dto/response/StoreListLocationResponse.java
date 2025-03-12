package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StoreListLocationResponse {
    private Long listId;
    private Long iconColorId;
    private Long storeId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
