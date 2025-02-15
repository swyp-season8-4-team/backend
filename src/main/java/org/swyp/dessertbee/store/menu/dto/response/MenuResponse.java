package org.swyp.dessertbee.store.menu.dto.response;

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
    private UUID menuUuid;
    private String name;
    private BigDecimal price;
    private Boolean isPopular;
    private String description;
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
