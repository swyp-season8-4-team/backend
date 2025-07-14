package org.swyp.dessertbee.store.store.service;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.swyp.dessertbee.common.util.SearchUtil;
import org.swyp.dessertbee.preference.exception.PreferenceExceptions.*;
import org.swyp.dessertbee.search.dto.StoreSearchResponse;
import org.swyp.dessertbee.statistics.store.event.StoreViewEvent;
import org.swyp.dessertbee.store.link.service.StoreLinkService;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.notice.dto.response.StoreNoticeResponse;
import org.swyp.dessertbee.store.notice.service.StoreNoticeService;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.store.preference.dto.TopPreferenceTagResponse;
import org.swyp.dessertbee.store.preference.repository.StoreTopTagRepository;
import org.swyp.dessertbee.store.schedule.dto.HolidayResponse;
import org.swyp.dessertbee.store.schedule.dto.OperatingHourResponse;
import org.swyp.dessertbee.store.schedule.service.StoreScheduleService;
import org.swyp.dessertbee.store.store.handler.StoreImageHandler;
import org.swyp.dessertbee.store.tag.dto.StoreTagResponse;
import org.swyp.dessertbee.store.tag.service.StoreTagService;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.response.MateResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewSummaryResponse;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.store.dto.response.*;
import org.swyp.dessertbee.store.store.entity.*;
import org.swyp.dessertbee.store.store.repository.*;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * StoreService 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final ImageService imageService;
    private final MenuService menuService;
    private final UserService userService;
    private final StoreNoticeService storeNoticeService;
    private final ApplicationEventPublisher eventPublisher;
    private final StoreTopTagRepository storeTopTagRepository;
    //private final ElasticsearchClient client;
    private final StoreSupportService storeSupportService;
    private final StoreLinkService storeLinkService;
    private final StoreScheduleService storeScheduleService;
    private final StoreImageHandler storeImageHandler;
    private final StoreTagService storeTagService;

    /**
     * 업주가 등록한 가게 (id, uuid, name) 리스트 조회
     */
    @Override
    public List<StoreShortInfoResponse> getStoresByOwnerUuid(UUID ownerUuid) {
        List<Store> stores = storeRepository.findAllByOwnerUuidAndDeletedAtIsNull(ownerUuid);

        if (stores.isEmpty()) {
            log.info("해당 업주가 등록한 가게가 없습니다 - ownerUuid: {}", ownerUuid);
            return Collections.emptyList();
        }

        return stores.stream()
                .map(store -> new StoreShortInfoResponse(
                        store.getStoreId(),
                        store.getStoreUuid(),
                        store.getName()))
                .toList();
    }

    /** 반경 내 가게 조회 */
    @Override
    public List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius) {
        try {
            List<Store> stores = storeRepository.findStoresByLocation(lat, lng, radius);

            if (stores.isEmpty()) {
                return Collections.emptyList();
            }

            // 가게 ID 리스트 추출
            List<Long> storeIds = stores.stream()
                    .map(Store::getStoreId)
                    .collect(Collectors.toList());

            // 모든 관련 데이터를 배치로 한 번에 조회
            Map<Long, List<String>> storeImagesMap = storeImageHandler.getStoreImageUrlsBatch(storeIds);
            Map<Long, List<OperatingHourResponse>> operatingHoursMap = storeScheduleService.getOperatingHoursBatch(storeIds);
            Map<Long, List<HolidayResponse>> holidaysMap = storeScheduleService.getHolidaysBatch(storeIds);
            Map<Long, Integer> reviewCountMap = storeReviewRepository.getReviewCountsBatch(storeIds);
            Map<Long, List<String>> tagsMap = storeTagService.getTagNamesBatch(storeIds);

            return stores.stream()
                    .map(store -> convertToStoreMapResponseOptimized(
                            store,
                            operatingHoursMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                            holidaysMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                            reviewCountMap.getOrDefault(store.getStoreId(), 0),
                            tagsMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                            storeImagesMap.getOrDefault(store.getStoreId(), Collections.emptyList())
                    ))
                    .collect(Collectors.toList());

        } catch (StoreMapReadException e) {
            log.warn("반경 내 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 특정 취향 태그를 가지는 가게 조회 */
    @Override
    public List<StoreMapResponse> getStoresByLocationAndTags(Double lat, Double lng, Double radius, List<Long> preferenceTagIds) {
        try{
            if (preferenceTagIds == null || preferenceTagIds.isEmpty()) {
                throw new PreferencesNotFoundException("취향 태그가 선택되지 않았습니다.");
            }

            // 여러 태그 중 하나라도 매칭되는 가게를 조회
            List<Store> stores = storeRepository.findStoresByLocationAndTags(lat, lng, radius, preferenceTagIds);

            return convertStoresToMapResponses(stores);
        } catch (StoreMapReadException e){
            log.warn("반경 내 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 가게 조회 및 검색 */
    @Override
    public List<StoreMapResponse> getStoresByLocationAndKeyword(Double lat, Double lng, Double radius, String searchKeyword) {
        try{
            String transformed = SearchUtil.toBooleanFulltextQuery(searchKeyword);
            List<Store> stores = storeRepository.findStoresByLocationAndKeyword(lat, lng, radius, transformed);

            return convertStoresToMapResponses(stores);
        } catch (StoreSearchFailedException e){
            log.warn("반경 내 가게 검색 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 검색 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 검색 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 사용자 취향 태그에 해당하는 가게 조회 */
    @Override
    public List<StoreMapResponse> getStoresByMyPreferences(Double lat, Double lng, Double radius) {
        try{
            UserEntity user = userService.getCurrentUser();
            // 인증된 사용자가 아닌 경우 예외 발생
            if (user == null) {
                throw new InvalidUserStatusException("인증되지 않은 사용자입니다.");
            }

            // 해당 사용자에게 취향(preference) 값이 존재하는지 확인
            List<Long> preferenceTagIds = user.getUserPreferences().stream()
                    .map(up -> up.getPreference().getId())
                    .distinct()
                    .toList();

            if (preferenceTagIds.isEmpty()) {
                throw new UserPreferencesNotFoundException();
            }

            // 사용자의 취향 태그 중 하나라도 매칭되는 가게 조회
            List<Store> stores = storeRepository.findStoresByLocationAndTags(lat, lng, radius, preferenceTagIds);

            return convertStoresToMapResponses(stores);
        } catch (PreferenceStoreReadException e){
            log.warn("반경 내 사용자 취향 맞춤 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 검색어 조건에 맞는 가게 검색 */
    @Override
    public List<StoreSearchResponse> searchStores(String keyword) {
        try {
            String transformed = SearchUtil.toBooleanFulltextQuery(keyword);
            List<Store> stores = storeRepository.findStoresByKeyword(transformed);

            return stores.stream()
                    .map(store -> {
                        List<StoreImageResponse> storeImages = storeImageHandler.getStoreImages(store.getStoreId());
                        String thumbnail = storeImages.isEmpty() ? null : storeImages.get(0).getUrl();

                        return StoreSearchResponse.builder()
                                .storeId(store.getStoreId())
                                .storeUuid(store.getStoreUuid())
                                .name(store.getName())
                                .address(store.getAddress())
                                .thumbnail(thumbnail)
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("가게 검색 중 오류 발생", e);
            throw new StoreServiceException("가게 검색 처리 중 오류가 발생했습니다.");
        }
    }

//    /**
//     * 검색결과와 정확히 일치하는 전체 가게 조회 (match_phrase 사용)
//     */
//    public List<StoreSearchResponse> searchStores(String keyword) {
//        try {
//            SearchResponse<StoreDocument> response = client.search(s -> s
//                            .index("stores")
//                            .query(q -> q
//                                    .bool(b -> b
//                                            .should(sh -> sh.matchPhrase(m -> m.field("storeName").query(keyword)))
//                                            .should(sh -> sh.matchPhrase(m -> m.field("address").query(keyword)))
//                                            .should(sh -> sh.matchPhrase(m -> m.field("menuNames").query(keyword)))
//                                            .should(sh -> sh.matchPhrase(m -> m.field("tagNames").query(keyword)))
//                                            .minimumShouldMatch("1")
//                                            .filter(f -> f.term(t -> t.field("deleted").value(false)))
//                                    )
//                            ),
//                    StoreDocument.class
//            );
//
//            return response.hits().hits().stream()
//                    .map(Hit::source)
//                    .filter(Objects::nonNull)
//                    .map(doc -> {
//                        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, doc.getStoreId());
//                        String thumbnail = storeImages.isEmpty() ? null : storeImages.get(0);
//
//                        return StoreSearchResponse.builder()
//                                .storeId(doc.getStoreId())
//                                .storeUuid(doc.getStoreUuid())
//                                .name(doc.getStoreName())
//                                .address(doc.getAddress())
//                                .thumbnail(thumbnail)
//                                .build();
//                    })
//                    .toList();
//
//        } catch (IOException e) {
//            log.error("Elasticsearch 검색 중 네트워크 오류 발생", e);
//            throw new ElasticsearchCommunicationException(
//                    "Elasticsearch 검색 중 IOException 발생", e
//            );
//        }
//    }

    private List<StoreMapResponse> convertStoresToMapResponses(List<Store> stores) {
        if (stores.isEmpty()) {
            return Collections.emptyList();
        }

        // getStoresByLocation()에 있는 로직을 그대로 복사
        List<Long> storeIds = stores.stream()
                .map(Store::getStoreId)
                .collect(Collectors.toList());

        Map<Long, List<String>> storeImagesMap = storeImageHandler.getStoreImageUrlsBatch(storeIds);
        Map<Long, List<OperatingHourResponse>> operatingHoursMap = storeScheduleService.getOperatingHoursBatch(storeIds);
        Map<Long, List<HolidayResponse>> holidaysMap = storeScheduleService.getHolidaysBatch(storeIds);
        Map<Long, Integer> reviewCountMap = storeReviewRepository.getReviewCountsBatch(storeIds);
        Map<Long, List<String>> tagsMap = storeTagService.getTagNamesBatch(storeIds);

        return stores.stream()
                .map(store -> convertToStoreMapResponseOptimized(
                        store,
                        operatingHoursMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                        holidaysMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                        reviewCountMap.getOrDefault(store.getStoreId(), 0),
                        tagsMap.getOrDefault(store.getStoreId(), Collections.emptyList()),
                        storeImagesMap.getOrDefault(store.getStoreId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 최적화된 StoreMapResponse 변환 메서드
     */
    private StoreMapResponse convertToStoreMapResponseOptimized(
            Store store,
            List<OperatingHourResponse> operatingHours,
            List<HolidayResponse> holidays,
            Integer totalReviewCount,
            List<String> tags,
            List<String> storeImages) {

        return StoreMapResponse.fromEntity(
                store,
                operatingHours,
                holidays,
                totalReviewCount,
                tags,
                storeImages
        );
    }

    /** 가게 간략 정보 조회 */
    @Override
    public StoreSummaryResponse getStoreSummary(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            List<StoreImageResponse> storeImages = storeImageHandler.getStoreImages(storeId);
            List<StoreImageResponse> ownerPickImages = storeImageHandler.getOwnerPickImages(storeId);
            List<String> tags = storeTagService.getTagNames(storeId);

            // 가게 링크 및 대표 링크 조회
            Pair<List<String>, String> linkInfo = storeLinkService.getStoreLinksAndPrimary(storeId);

            // 운영 시간 조회
            List<OperatingHourResponse> operatingHourResponses = storeScheduleService.getOperatingHoursResponse(storeId);

            // 휴무일 조회
            List<HolidayResponse> holidayResponses = storeScheduleService.getHolidaysResponse(storeId);

            // 가게 취향 태그 top3 조회
            List<TopPreferenceTagResponse> topPreferences = storeTopTagRepository.findTop3TagsByStoreId(storeId);

            return StoreSummaryResponse.fromEntity(
                    store,
                    tags,
                    linkInfo.getLeft(),
                    linkInfo.getRight(),
                    operatingHourResponses,
                    holidayResponses,
                    storeImages,
                    ownerPickImages,
                    topPreferences
            );
        } catch (StoreInfoReadFailedException e){
            log.warn("가게 간략정보 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 간략정보 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 간략정보 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 상세 정보 조회 */
    @Override
    public StoreDetailResponse getStoreDetails(UUID storeUuid) {
        try {
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            // 사용자 정보 조회
            UserEntity user = userService.getCurrentUser();
            Long userId = (user != null) ? user.getId() : null;
            UUID userUuid = (user != null) ? user.getUserUuid() : null;

            // 비동기로 이미지 조회
            CompletableFuture<List<StoreImageResponse>> storeImagesFuture =
                    CompletableFuture.supplyAsync(() -> storeImageHandler.getStoreImages(storeId));

            CompletableFuture<List<StoreImageResponse>> ownerPickImagesFuture =
                    CompletableFuture.supplyAsync(() -> storeImageHandler.getOwnerPickImages(storeId));

            // 사용자별 데이터 조회
            Pair<Boolean, Long> savedInfo = storeSupportService.getUserStoreSavedInfo(store, user);
            boolean saved = savedInfo.getLeft();
            Long savedListId = savedInfo.getRight();

            List<StoreTagResponse> tags = storeTagService.getTagResponses(storeId);
            Pair<List<String>, String> linkInfo = storeLinkService.getStoreLinksAndPrimary(storeId);
            List<OperatingHourResponse> operatingHourResponses = storeScheduleService.getOperatingHoursResponse(storeId);
            List<HolidayResponse> holidayResponses = storeScheduleService.getHolidaysResponse(storeId);
            List<StoreNoticeResponse> noticeResponses = storeNoticeService.getNoticesByStoreUuid(storeUuid);
            List<TopPreferenceTagResponse> topPreferences = storeSupportService.getTop3Preferences(storeId);
            List<MenuResponse> menus = menuService.getMenusByStore(storeUuid);
            List<StoreReviewResponse> reviewResponses = storeSupportService.getStoreReviewResponses(storeId);
            int totalReviewCount = reviewResponses.size();
            List<ReviewSummaryResponse> communityResponses = storeSupportService.getCommunityReviewResponses(storeId);
            List<MateResponse> mateResponses = storeSupportService.getMateResponses(storeId, userId);

            // 이미지 로딩 완료 대기
            List<StoreImageResponse> storeImages = storeImagesFuture.join();
            List<StoreImageResponse> ownerPickImages = ownerPickImagesFuture.join();

            // 트랜잭션 커밋 후 이벤트 발행
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publishEvent(new StoreViewEvent(storeId, userUuid));
                        }
                    }
            );

            return StoreDetailResponse.fromEntity(
                    store, userId, userUuid, totalReviewCount,
                    operatingHourResponses, holidayResponses, noticeResponses,
                    menus, storeImages, ownerPickImages, topPreferences,
                    reviewResponses, tags, linkInfo.getLeft(), linkInfo.getRight(),
                    communityResponses, mateResponses, saved, savedListId
            );

        } catch (StoreInfoReadFailedException e) {
            log.warn("가게 상세정보 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 상세정보 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 상세정보 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
    @Override
    public void updateAverageRating(Long storeId) {
        try{
            BigDecimal newAverageRating = storeReviewRepository.findAverageRatingByStoreId(storeId);
            storeRepository.updateAverageRating(storeId, newAverageRating);
        } catch (StoreRateUpdateException e){
            log.warn("가게 평균 평점 업데이트 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 평균 평점 업데이트 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 평균 평점 업데이트 처리 중 오류가 발생했습니다.");
        }
    }
}
