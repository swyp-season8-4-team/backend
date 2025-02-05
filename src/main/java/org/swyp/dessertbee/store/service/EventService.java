package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.dto.response.EventResponse;
import org.swyp.dessertbee.store.entity.Event;
import org.swyp.dessertbee.store.repository.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;

    /** 특정 가게의 이벤트 목록 조회 */
    public List<EventResponse> getEventsByStore(Long storeId) {
        List<Event> events = eventRepository.findByStoreIdOrderByStartDateAsc(storeId);

        return events.stream()
                .map(event -> {
                    List<String> images = imageService.getImagesByTypeAndId(ImageType.EVENT, event.getId());
                    return EventResponse.fromEntity(event, images);
                })
                .collect(Collectors.toList());
    }

    /** 특정 가게의 특정 이벤트 조회 */
    public EventResponse getEventByStore(Long storeId, Long eventId) {
        Event event = eventRepository.findByIdAndStoreId(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 이벤트입니다."));

        List<String> images = imageService.getImagesByTypeAndId(ImageType.EVENT, eventId);

        return EventResponse.fromEntity(event, images);
    }

    /** 단일 이벤트 추가 */
    public void addEvent(Long storeId, EventCreateRequest request, List<MultipartFile> files) {
        boolean exists = eventRepository.existsByStoreIdAndTitleAndStartDate(storeId, request.getTitle(), request.getStartDate());

        if (exists) {
            throw new IllegalArgumentException("이미 등록된 이벤트입니다.");
        }

        Event event = eventRepository.save(Event.builder()
                .storeId(storeId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build());

        if (files != null && !files.isEmpty()) {
            imageService.uploadAndSaveImages(files, ImageType.EVENT, event.getId(), "event/" + event.getId());
        }
    }

    /** 여러 개의 이벤트 추가 */
    public void addEvents(Long storeId, List<EventCreateRequest> eventRequests, Map<String, List<MultipartFile>> eventImageFiles) {
        if (eventRequests == null || eventRequests.isEmpty()) return;

        List<Event> events = eventRequests.stream()
                .map(request -> Event.builder()
                        .storeId(storeId)
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build())
                .collect(Collectors.toList());

        eventRepository.saveAll(events);

        // 각 이벤트에 이미지 업로드
        events.forEach(event -> {
            List<MultipartFile> files = eventImageFiles.get(event.getTitle());
            if (files != null && !files.isEmpty()) {
                imageService.uploadAndSaveImages(files, ImageType.EVENT, event.getId(), "event/" + event.getId());
            }
        });
    }

    /** 이벤트 수정 */
    public void updateEvent(Long storeId, Long eventId, EventCreateRequest request, List<Long> deleteImageIds, List<MultipartFile> files) {
        Event event = eventRepository.findByIdAndStoreId(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        event.update(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());

        imageService.updatePartialImages(deleteImageIds, files, ImageType.EVENT, eventId, "event/" + eventId);
    }

    /** 이벤트 삭제 */
    public void deleteEvent(Long storeId, Long eventId) {
        Event event = eventRepository.findByIdAndStoreId(eventId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        eventRepository.delete(event);
        imageService.deleteImagesByRefId(ImageType.EVENT, eventId);
    }
}
