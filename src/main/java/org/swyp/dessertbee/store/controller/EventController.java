package org.swyp.dessertbee.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.dto.response.EventResponse;
import org.swyp.dessertbee.store.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /** 특정 가게의 이벤트 목록 조회 */
    @GetMapping
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

    /** 이벤트 추가 (파일 업로드 포함) */
    @PostMapping
    public ResponseEntity<Void> addEvent(
            @PathVariable Long storeId,
            @RequestPart("request") EventCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        eventService.addEvent(storeId, request, files);
        return ResponseEntity.ok().build();
    }

    /** 이벤트 수정 (파일 업로드 포함) */
    @PutMapping("/{eventId}")
    public ResponseEntity<Void> updateEvent(
            @PathVariable Long storeId,
            @PathVariable Long eventId,
            @RequestPart("request") EventCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        eventService.updateEvent(storeId, eventId, request, files);
        return ResponseEntity.ok().build();
    }

    /** 이벤트 삭제 */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long storeId, @PathVariable Long eventId) {
        eventService.deleteEvent(storeId, eventId);
        return ResponseEntity.ok().build();
    }
}
