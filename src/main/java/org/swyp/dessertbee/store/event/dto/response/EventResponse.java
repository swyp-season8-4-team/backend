package org.swyp.dessertbee.store.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.event.entity.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class EventResponse {
    private UUID eventUuid;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> images;

    public static EventResponse fromEntity(Event event, List<String> images) {
        return new EventResponse(
                event.getEventUuid(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                images
        );
    }
}
