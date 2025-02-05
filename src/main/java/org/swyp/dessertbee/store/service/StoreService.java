package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.model.Identifiable;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.common.service.S3Service;
import org.swyp.dessertbee.store.dto.request.CouponCreateRequest;
import org.swyp.dessertbee.store.dto.request.EventCreateRequest;
import org.swyp.dessertbee.store.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.response.*;
import org.swyp.dessertbee.store.entity.*;
import org.swyp.dessertbee.store.repository.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreTagRepository storeTagRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final MenuRepository menuRepository;
    private final EventRepository eventRepository;
    private final CouponRepository couponRepository;
    private final StoreStatisticsRepository storeStatisticsRepository;
    private final ImageService imageService;
    private final S3Service s3Service;
    private final MenuService menuService;
    private final EventService eventService;

    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    public StoreDetailResponse createStore(StoreCreateRequest request, List<MultipartFile> storeImageFiles) {
        // 가게 정보 저장
        Store store = storeRepository.save(
                Store.builder()
                        .ownerId(request.getOwnerId())
                        .name(request.getName())
                        .phone(request.getPhone())
                        .address(request.getAddress())
                        .storeLink(request.getStoreLink())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .description(request.getDescription())
                        .animalYn(Boolean.TRUE.equals(request.getAnimalYn()))
                        .tumblerYn(Boolean.TRUE.equals(request.getTumblerYn()))
                        .parkingYn(Boolean.TRUE.equals(request.getParkingYn()))
                        .operatingHours(request.getOperatingHours())
                        .closingDays(request.getClosingDays())
                        .build()
        );

        // 가게 통계 초기화
        storeStatisticsRepository.save(
                StoreStatistics.builder()
                        .storeId(store.getId())
                        .views(0)
                        .saves(0)
                        .reviews(0)
                        .build()
        );

        // 가게 대표 사진 S3 업로드 및 저장
        if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
            String folder = "store/" + store.getId();
            imageService.uploadAndSaveImages(storeImageFiles, ImageType.STORE, store.getId(), folder);
        }

        // 태그 저장
        saveStoreTags(store, request.getTagIds());

        // 메뉴 저장 (한 메뉴당 하나의 이미지만 업로드 가능)
        menuService.addMenus(store.getId(), request.getMenus(), request.getMenuImageFiles());

        // 이벤트 저장 (한 이벤트당 여러 개의 이미지 업로드 가능)
        eventService.addEvents(store.getId(), request.getEvents(), request.getEventImageFiles());

        // 쿠폰 저장
        saveCoupons(store, request.getCoupons());

        return getStoreDetails(store.getId());
    }

    /** 태그 저장 (1~3개 선택) */
    private void saveStoreTags(Store store, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty() || tagIds.size() > 3) {
            throw new IllegalArgumentException("태그는 1개 이상 3개 이하로 선택해야 합니다.");
        }

        // 선택한 태그 조회
        List<StoreTag> selectedTags = storeTagRepository.findByIdIn(tagIds);

        // 태그가 유효한지 검증 (혹시 존재하지 않는 태그 ID가 포함되었는지 체크)
        if (selectedTags.size() != tagIds.size()) {
            throw new IllegalArgumentException("유효하지 않은 태그가 포함되어 있습니다.");
        }

        // 태그-가게 관계 저장
        List<StoreTagRelation> tagRelations = selectedTags.stream()
                .map(tag -> StoreTagRelation.builder()
                        .store(store)
                        .tag(tag)
                        .build())
                .collect(Collectors.toList());

        storeTagRelationRepository.saveAll(tagRelations);
    }

    /** 특정 가게의 쿠폰 목록 조회 */
    public List<CouponResponse> getCouponsByStore(Long storeId) {
        return couponRepository.findByStoreId(storeId)
                .stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 쿠폰 저장 */
    private void saveCoupons(Store store, List<CouponCreateRequest> coupons) {
        if (coupons == null || coupons.isEmpty()) return;

        couponRepository.saveAll(
                coupons.stream()
                        .map(coupon -> Coupon.builder()
                                .storeId(store.getId())
                                .title(coupon.getTitle())
                                .description(coupon.getDescription())
                                .expiryDate(coupon.getExpiryDate())
                                .build())
                        .collect(Collectors.toList())
        );
    }

    /** 반경 내 가게 조회 */
    public List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius) {
        List<Store> stores = storeRepository.findStoresByLocation(lat, lng, radius);

        return stores.stream()
                .map(StoreMapResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 가게 간략 정보 조회 */
    public StoreSummaryResponse getStoreSummary(Long storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 가게입니다."));

        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

        return StoreSummaryResponse.fromEntity(store, tags, storeImages);
    }

    /** 가게 상세 정보 조회 */
    public StoreDetailResponse getStoreDetails(Long storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 가게입니다."));

        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
        List<EventResponse> events = eventService.getEventsByStore(storeId);
        List<MenuResponse> menus = menuService.getMenusByStore(storeId);
        List<CouponResponse> coupons = getCouponsByStore(storeId);  // ✅ 쿠폰 목록 추가
        // 리뷰 ID 목록을 가져와서 해당 리뷰들의 이미지 URL 매핑

        // 리뷰 기능 개발 후 수정할 부분 (미완성)
        List<StoreReview> reviews = storeReviewRepository.findByStoreId(storeId);
        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.REVIEW,
                reviews.stream().map(StoreReview::getId).toList());
        List<StoreReviewResponse> reviewResponses = reviews.stream()
                .map(review -> StoreReviewResponse.fromEntity(review, reviewImagesMap.getOrDefault(review.getId(), List.of())))
                .toList();
        //
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

        return StoreDetailResponse.fromEntity(store, events, menus, coupons, storeImages, reviewResponses, tags);
    }


    /** 이미지 조회 */
    private <T extends Identifiable> List<String> getImageUrlsForEntities(List<T> entities, ImageType imageType) {
        return entities.stream()
                .map(entity -> imageService.getImagesByTypeAndId(imageType, entity.getId()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /** 리뷰 응답 객체 변환 */
    private List<StoreReviewResponse> getStoreReviewResponses(List<StoreReview> reviews) {
        if (reviews.isEmpty()) return Collections.emptyList();

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = reviews.stream().map(StoreReview::getId).collect(Collectors.toList());

        // 리뷰별 이미지 조회
        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.SHORT, reviewIds);

        // 리뷰 객체 변환
        return reviews.stream()
                .map(review -> StoreReviewResponse.fromEntity(review, reviewImagesMap.getOrDefault(review.getId(), List.of())))
                .collect(Collectors.toList());
    }

    /** 가게의 평균 평점 업데이트 (리뷰 등록/삭제 시 호출) */
    @Transactional
    public void updateAverageRating(Long storeId) {
        BigDecimal newAverageRating = storeReviewRepository.findAverageRatingByStoreId(storeId);
        storeRepository.updateAverageRating(storeId, newAverageRating);
    }
}
