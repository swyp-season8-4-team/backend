package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.dto.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.StoreResponse;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.entity.StoreTag;
import org.swyp.dessertbee.store.entity.StoreTagRelation;
import org.swyp.dessertbee.store.repository.StoreRepository;
import org.swyp.dessertbee.store.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.repository.StoreTagRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreTagRepository storeTagRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;

    /** 가게 등록 */
    public StoreResponse createStore(StoreCreateRequest request) {
        Store store = Store.builder()
                .ownerId(request.getOwnerId())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .storeLink(request.getStoreLink())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .animalYn(request.getAnimalYn() != null ? request.getAnimalYn() : false)
                .tumblerYn(request.getTumblerYn() != null ? request.getTumblerYn() : false)
                .parkingYn(request.getParkingYn() != null ? request.getParkingYn() : false)
                .operatingHours(request.getOperatingHours())
                .closingDays(request.getClosingDays())
                .status(request.getStatus())
                .build();

        store = storeRepository.save(store);

        // 태그 추가 (기존에 없는 것만)
        List<StoreTagRelation> tagRelations = createTagRelations(store, request.getTags());
        storeTagRelationRepository.saveAll(tagRelations);

        return toResponse(store);
    }

    /** 태그 관계 생성 */
    private List<StoreTagRelation> createTagRelations(Store store, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return List.of();

        // 중복 태그 제거
        Set<String> uniqueTagNames = Set.copyOf(tagNames);

        // 기존 태그를 한 번에 조회
        Map<String, StoreTag> existingTags = storeTagRepository.findByNameIn(uniqueTagNames)
                .stream()
                .collect(Collectors.toMap(StoreTag::getName, tag -> tag));

        // 없는 태그만 저장
        List<StoreTag> newTags = uniqueTagNames.stream()
                .filter(tagName -> !existingTags.containsKey(tagName)) // 기존 태그에 없는 것만 필터링
                .map(tagName -> StoreTag.builder().name(tagName).build()) // 새로운 태그 생성
                .toList();

        if (!newTags.isEmpty()) {  // 새 태그가 있을 때만 saveAll() 실행
            List<StoreTag> savedTags = storeTagRepository.saveAll(newTags);
            savedTags.forEach(tag -> existingTags.put(tag.getName(), tag));  // 기존 태그 Map에 추가
        }

        // 태그-가게 관계 생성
        return uniqueTagNames.stream()
                .map(tagName -> StoreTagRelation.builder()
                        .store(store)
                        .tag(existingTags.get(tagName))
                        .build())
                .toList();
    }

    /** Store -> StoreResponse 변환 */
    private StoreResponse toResponse(Store store) {
        List<String> tagNames = storeTagRelationRepository.findByStore(store).stream()
                .map(relation -> relation.getTag().getName())
                .toList();

        return new StoreResponse(store, tagNames);
    }
}
