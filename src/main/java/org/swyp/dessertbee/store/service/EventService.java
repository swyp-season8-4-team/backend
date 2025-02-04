package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.dto.response.EventResponse;
import org.swyp.dessertbee.store.entity.Event;
import org.swyp.dessertbee.store.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;

    /** 특정 가게의 특정 이벤트 조회 */
    public EventResponse getEventByStore(Long storeId, Long eventId) {
        Event event = eventRepository.findByIdAndStoreId(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 이벤트입니다."));
        return EventResponse.fromEntity(event);
    }

    /** 이벤트 추가 */
    public void addEvent(Long storeId, EventCreateRequest request){
        Event event = Event.builder()
                .storeId(storeId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        eventRepository.save(event);
    }

    /** 이벤트 수정 */
    public void updateEvent(Long eventId, EventCreateRequest request){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        event.update(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
    }

    /** 이벤트 삭제 */
    public void deleteEvent(Long eventId){
        eventRepository.deleteById(eventId);
    }

    /** 특정 가게의 이벤트 목록 조회 */
    public List<EventResponse> getEventsByStore(Long storeId) {
        List<Event> events = eventRepository.findByStoreId(storeId);
        return events.stream().map(EventResponse::fromEntity).collect(Collectors.toList());
    }
}
