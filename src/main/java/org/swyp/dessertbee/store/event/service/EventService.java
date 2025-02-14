package org.swyp.dessertbee.store.event.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.event.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.event.dto.response.EventResponse;
import org.swyp.dessertbee.store.event.entity.Event;
import org.swyp.dessertbee.store.event.repository.EventRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;
    private final StoreRepository storeRepository;

    /** 특정 가게의 이벤트 목록 조회 */
    public List<EventResponse> getEventsByStore(UUID storeUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        List<Event> events = eventRepository.findByStoreIdAndDeletedAtIsNullOrderByStartDateAsc(storeId);

        return events.stream()
                .map(event -> {
                    List<String> images = imageService.getImagesByTypeAndId(ImageType.EVENT, event.getEventId());
                    return EventResponse.fromEntity(event, images);
                })
                .collect(Collectors.toList());
    }

    /** 특정 가게의 특정 이벤트 조회 */
    public EventResponse getEventByStore(UUID storeUuid, UUID eventUuid) {
        Long eventId = eventRepository.findEventIdByEventUuid(eventUuid);
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Event event = eventRepository.findByEventIdAndStoreIdAndDeletedAtIsNull(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 이벤트입니다."));

        List<String> images = imageService.getImagesByTypeAndId(ImageType.EVENT, eventId);

        return EventResponse.fromEntity(event, images);
    }

    /** 이벤트 추가 */
    @Transactional
    public List<Event> addEvents(UUID storeUuid, List<EventCreateRequest> eventRequests, Map<Long, List<MultipartFile>> eventImageFiles) {
        if (eventRequests == null || eventRequests.isEmpty()) return List.of();
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);

        List<Event> events = eventRequests.stream()
                .map(request -> {
                    Event event = Event.builder()
                            .storeId(storeId)
                            .title(request.getTitle())
                            .description(request.getDescription())
                            .startDate(request.getStartDate())
                            .endDate(request.getEndDate())
                            .build();
                    return eventRepository.save(event);
                })
                .toList();

        events.forEach(event -> {
            Long eventId = eventRepository.findEventIdByEventUuid(event.getEventUuid());
            log.info("🔍 저장된 이벤트 ID: " + eventId);
            log.info("🔍 eventImageFiles 키 목록: " + eventImageFiles.keySet());

            List<MultipartFile> files = eventImageFiles.get(eventId);
            if (files == null || files.isEmpty()) {
                log.info("⚠️ 이벤트 ID " + eventId + "에 대한 이미지가 없음");
            } else {
                log.info("✅ 이벤트 ID " + eventId + "에 대한 이미지 " + files.size() + "개 저장 중...");
                imageService.uploadAndSaveImages(files, ImageType.EVENT, eventId, "event/" + eventId);
            }
        });


        return events;
    }

    /** 이벤트 수정 */
    public void updateEvent(UUID storeUuid, UUID eventUuid, EventCreateRequest request, List<Long> deleteImageIds, List<MultipartFile> files) {
        Long eventId = eventRepository.findEventIdByEventUuid(eventUuid);
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);

        Event event = eventRepository.findByEventIdAndStoreIdAndDeletedAtIsNull(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        event.update(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());

        // 기존 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            imageService.deleteImagesByIds(deleteImageIds);
        }

        // 새로운 이미지 추가
        if (files != null && !files.isEmpty()) {
            imageService.uploadAndSaveImages(files, ImageType.EVENT, eventId, "event/" + eventId);
        }
    }

    /** 이벤트 삭제 */
    public void deleteEvent(UUID storeUuid, UUID eventUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long eventId = eventRepository.findEventIdByEventUuid(eventUuid);
        Event event = eventRepository.findByEventIdAndStoreIdAndDeletedAtIsNull(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        event.softDelete();
        eventRepository.save(event);
        imageService.deleteImagesByRefId(ImageType.EVENT, eventId);
    }
}
