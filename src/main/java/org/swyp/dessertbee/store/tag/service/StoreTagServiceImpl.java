package org.swyp.dessertbee.store.tag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.tag.dto.StoreTagResponse;
import org.swyp.dessertbee.store.tag.entity.StoreTag;
import org.swyp.dessertbee.store.tag.entity.StoreTagRelation;
import org.swyp.dessertbee.store.store.exception.StoreExceptions;
import org.swyp.dessertbee.store.tag.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.tag.repository.StoreTagRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreTagServiceImpl implements StoreTagService {

    private final StoreTagRepository storeTagRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;

    /**
     * 태그 저장 (1~3개 선택)
     */
    @Override
    public void saveStoreTags(Store store, List<Long> tagIds) {
        try {
            if (tagIds == null || tagIds.isEmpty() || tagIds.size() > 3) {
                log.warn("태그 선택 갯수 오류");
                throw new StoreExceptions.InvalidTagSelectionException("태그 선택 갯수가 잘못되었습니다.");
            }

            // 선택한 태그 조회
            List<StoreTag> selectedTags = storeTagRepository.findByIdIn(tagIds);

            // 태그가 유효한지 검증 (혹시 존재하지 않는 태그 ID가 포함되었는지 체크)
            if (selectedTags.size() != tagIds.size()) {
                log.warn("존재하지 않는 태그 오류");
                throw new StoreExceptions.InvalidTagIncludedException("유효하지 않은 태그값이 포함되었습니다.");
            }

            // 태그-가게 관계 저장
            List<StoreTagRelation> tagRelations = selectedTags.stream()
                    .map(tag -> StoreTagRelation.builder()
                            .store(store)
                            .tag(tag)
                            .build())
                    .toList();

            storeTagRelationRepository.saveAll(tagRelations);
        } catch (StoreExceptions.StoreTagSaveFailedException e) {
            log.warn("태그 저장 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("태그 저장 처리 중 오류 발생", e);
            throw new StoreExceptions.StoreServiceException("태그 저장 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 태그 조회
     */
    @Override
    public List<StoreTagResponse> getTagResponses(Long storeId) {
        return storeTagRelationRepository.findTagsByStoreId(storeId)
                .stream()
                .map(StoreTagResponse::fromEntity)
                .toList();
    }

    /**
     * 태그 이름 조회
     */
    @Override
    public List<String> getTagNames(Long storeId) {
        return storeTagRelationRepository.findTagNamesByStoreId(storeId);
    }

    /**
     * 여러 가게의 태그명 배치 조회
     */
    public Map<Long, List<String>> getTagNamesBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 한 번의 쿼리로 모든 가게의 태그명 조회
        List<Object[]> results = storeTagRelationRepository.findTagNamesByStoreIds(storeIds);

        Map<Long, List<String>> tagMap = new HashMap<>();
        for (Object[] result : results) {
            Long storeId = (Long) result[0];
            String tagName = (String) result[1];

            tagMap.computeIfAbsent(storeId, k -> new ArrayList<>()).add(tagName);
        }

        // 태그가 없는 가게들도 빈 리스트로 초기화
        for (Long storeId : storeIds) {
            tagMap.putIfAbsent(storeId, new ArrayList<>());
        }

        return tagMap;
    }

    /**
     * 여러 가게의 태그 응답 배치 조회 (StoreTagResponse용)
     */
    public Map<Long, List<StoreTagResponse>> getTagResponsesBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = storeTagRelationRepository.findTagResponsesByStoreIds(storeIds);

        Map<Long, List<StoreTagResponse>> responseMap = new HashMap<>();
        for (Object[] result : results) {
            Long storeId = (Long) result[0];
            Long tagId = (Long) result[1];
            String tagName = (String) result[2];
            Long categoryId = (Long) result[3];
            String categoryName = (String) result[4];

            StoreTagResponse tagResponse = StoreTagResponse.builder()
                    .id(tagId)
                    .name(tagName)
                    .category(StoreTagResponse.TagCategoryResponse.builder()
                            .id(categoryId)
                            .name(categoryName)
                            .build())
                    .build();

            responseMap.computeIfAbsent(storeId, k -> new ArrayList<>()).add(tagResponse);
        }

        // 태그가 없는 가게들도 빈 리스트로 초기화
        for (Long storeId : storeIds) {
            responseMap.putIfAbsent(storeId, new ArrayList<>());
        }

        return responseMap;
    }

    /**
     * Fetch Join 사용
     */
    public Map<Long, List<String>> getTagNamesBatchWithFetchJoin(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<StoreTagRelation> relations = storeTagRelationRepository.findByStoreIdInWithTagAndCategory(storeIds);

        return relations.stream()
                .collect(Collectors.groupingBy(
                        relation -> relation.getStore().getStoreId(),
                        Collectors.mapping(
                                relation -> relation.getTag().getName(),
                                Collectors.toList()
                        )
                ));
    }
}
