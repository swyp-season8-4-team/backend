package org.swyp.dessertbee.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.dto.response.EventResponse;
import org.swyp.dessertbee.store.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /** 특정 가게의 이벤트 목록 조회 */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<EventResponse>> getEventsByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(eventService.getEventsByStore(storeId));
    }

    /** 특정 가게의 특정 이벤트 조회 */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventByStore(
            @PathVariable Long storeId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventByStore(storeId, eventId));
    }

    /** 이벤트 추가 */
    @PostMapping("/{storeId}")
    public ResponseEntity<Void> addEvent(@PathVariable Long storeId, @RequestBody EventCreateRequest request){
        eventService.addEvent(storeId, request);
        return ResponseEntity.ok().build();
    }

    /** 이벤트 수정 */
    @PutMapping("/{eventId}")
    public ResponseEntity<Void> updateEvent(@PathVariable Long eventId, @RequestBody EventCreateRequest request){
        eventService.updateEvent(eventId, request);
        return ResponseEntity.ok().build();
    }

    /** 이벤트 삭제 */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId){
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }
}
