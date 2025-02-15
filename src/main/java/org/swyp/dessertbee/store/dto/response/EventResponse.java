package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.common.model.Identifiable;
import org.swyp.dessertbee.store.entity.Event;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EventResponse implements Identifiable {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> images;

    @Override
    public Long getId() {
        return id;
    }

    public static EventResponse fromEntity(Event event, List<String> images) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                images
        );
    }
}
