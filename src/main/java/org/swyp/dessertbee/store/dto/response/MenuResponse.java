package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.common.model.Identifiable;
import org.swyp.dessertbee.store.entity.Menu;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MenuResponse implements Identifiable {
    private Long id;
    private String name;
    private BigDecimal price;
    private Boolean isPopular;
    private String description;
    private List<String> images;

    @Override
    public Long getId() {
        return id;
    }

    public static MenuResponse fromEntity(Menu menu, List<String> images) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getIsPopular(),
                menu.getDescription(),
                images
        );
    }
}
