package org.swyp.dessertbee.store.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.event.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.event.dto.response.EventResponse;
import org.swyp.dessertbee.store.event.repository.EventRepository;
import org.swyp.dessertbee.store.event.service.EventService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/stores/{storeUuid}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /** 특정 가게의 이벤트 목록 조회 */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsByStore(@PathVariable UUID storeUuid) {
        return ResponseEntity.ok(eventService.getEventsByStore(storeUuid));
    }

    /** 특정 가게의 특정 이벤트 조회 */
    @GetMapping("/{eventUuid}")
    public ResponseEntity<EventResponse> getEventByStore(
            @PathVariable UUID storeUuid,
            @PathVariable UUID eventUuid) {
        return ResponseEntity.ok(eventService.getEventByStore(storeUuid, eventUuid));
    }

    /** 이벤트 추가 */
    @PostMapping
    public ResponseEntity<Void> addEvents(
            @PathVariable UUID storeUuid,
            @RequestPart("requests") List<EventCreateRequest> eventRequests,
            @RequestPart(value = "eventImages", required = false) Map<Long, List<MultipartFile>> eventImageFiles) {

        if (eventRequests == null || eventRequests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (eventImageFiles == null) {
            eventImageFiles = Collections.emptyMap();
        }

        try {
            // 이벤트 추가 서비스 호출
            eventService.addEvents(storeUuid, eventRequests, eventImageFiles);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /** 이벤트 수정 */
    @PatchMapping("/{eventUuid}")
    public ResponseEntity<Void> updateEvent(
            @PathVariable UUID storeUuid,
            @PathVariable UUID eventUuid,
            @RequestPart("request") EventCreateRequest request,
            @RequestPart(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        eventService.updateEvent(storeUuid, eventUuid, request, deleteImageIds, files);
        return ResponseEntity.ok().build();
    }

    /** 이벤트 삭제 */
    @DeleteMapping("/{eventUuid}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID storeUuid, @PathVariable UUID eventUuid) {
        eventService.deleteEvent(storeUuid, eventUuid);
        return ResponseEntity.ok().build();
    }
}
