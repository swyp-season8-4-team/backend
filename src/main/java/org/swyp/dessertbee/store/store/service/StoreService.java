package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.response.*;
import org.swyp.dessertbee.store.store.entity.*;
import org.swyp.dessertbee.store.store.repository.*;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreTagRepository storeTagRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final StoreStatisticsRepository storeStatisticsRepository;
    private final StoreOperatingHourRepository storeOperatingHourRepository;
    private final StoreHolidayRepository storeHolidayRepository;
    private final SavedStoreRepository savedStoreRepository;
    private final ImageService imageService;
    private final MenuService menuService;
    private final UserRepository userRepository;

    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public StoreDetailResponse createStore(StoreCreateRequest request,
                                           List<MultipartFile> storeImageFiles,
                                           List<MultipartFile> ownerPickImageFiles,
                                           List<MultipartFile> menuImageFiles) {

        // ownerId로 UserEntity 조회 (로그인한 사용자 정보)
        UserEntity user = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (menuImageFiles == null) {
            menuImageFiles = Collections.emptyList(); // menuImageFiles가 null이면 빈 리스트로 처리
        }

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
                        .notice(request.getNotice())
                        .build()
        );

        // 가게 통계 초기화
        storeStatisticsRepository.save(
                StoreStatistics.builder()
                        .storeId(store.getStoreId())
                        .views(0)
                        .saves(0)
                        .reviews(0)
                        .build()
        );

        // 가게 대표 사진 S3 업로드 및 저장
        if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
            String folder = "store/" + store.getStoreId();
            imageService.uploadAndSaveImages(storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
        }

        // 사장님 픽 이미지 S3 업로드 및 저장
        if (ownerPickImageFiles != null && !ownerPickImageFiles.isEmpty()) {
            String folder = "ownerpick/" + store.getStoreId();
            imageService.uploadAndSaveImages(ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
        }

        // 태그 저장
        saveStoreTags(store, request.getTagIds());

        // 메뉴 이미지 파일을 Map<String, MultipartFile>로 변환 (이름을 키로 사용)
        Map<String, MultipartFile> menuImageMap = menuImageFiles.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> b));

        // 메뉴 저장 (한 메뉴당 하나의 이미지만 업로드 가능)
        menuService.addMenus(store.getStoreUuid(), request.getMenus(), menuImageMap);

        // 영업 시간 저장
        if (request.getOperatingHours() != null) {
            List<StoreOperatingHour> operatingHours = request.getOperatingHours().stream()
                    .map(hour -> StoreOperatingHour.builder()
                            .storeId(store.getStoreId())
                            .dayOfWeek(hour.getDayOfWeek())
                            .openingTime(hour.getOpeningTime())
                            .closingTime(hour.getClosingTime())
                            .lastOrderTime(hour.getLastOrderTime())
                            .isClosed(hour.getIsClosed())
                            .build())
                    .collect(Collectors.toList());
            storeOperatingHourRepository.saveAll(operatingHours);
        }

        // 휴무일 저장
        if (request.getHolidays() != null) {
            List<StoreHoliday> holidays = request.getHolidays().stream()
                    .map(holiday -> StoreHoliday.builder()
                            .storeId(store.getStoreId())
                            .holidayDate(LocalDate.parse(holiday.getDate()))
                            .reason(holiday.getReason())
                            .build())
                    .collect(Collectors.toList());
            storeHolidayRepository.saveAll(holidays);
        }

        return getStoreDetails(store.getStoreUuid(), user);
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

    /** 반경 내 가게 조회 */
    public List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius) {
        List<Store> stores = storeRepository.findStoresByLocation(lat, lng, radius);

        return stores.stream()
                .map(StoreMapResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 가게 간략 정보 조회 */
    public StoreSummaryResponse getStoreSummary(UUID storeUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 가게입니다."));

        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
        List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeUuid);

        // 운영 시간
        List<StoreOperatingHour> operatingHours = storeOperatingHourRepository.findByStoreId(storeId);
        List<OperatingHourResponse> operatingHourResponses = operatingHours.stream()
                .map(o -> OperatingHourResponse.builder()
                        .dayOfWeek(o.getDayOfWeek())
                        .openingTime(o.getOpeningTime())
                        .closingTime(o.getClosingTime())
                        .lastOrderTime(o.getLastOrderTime())
                        .isClosed(o.getIsClosed())
                        .build())
                .toList();

        // 휴무일
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreId(storeId);
        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> HolidayResponse.builder()
                        .date(h.getHolidayDate().toString())
                        .reason(h.getReason())
                        .build())
                .toList();

        // 해당 가게를 저장한 사람들의 취향 태그 Top3 조회
        List<Object[]> preferenceCounts = savedStoreRepository.findTop3PreferencesByStoreId(storeId);
        List<String> topPreferences = preferenceCounts.stream()
                .map(result -> (String) result[0])
                .toList();

        return  StoreSummaryResponse.fromEntity(store, tags, operatingHourResponses, holidayResponses, storeImages, ownerPickImages, topPreferences);
    }

    /** 가게 상세 정보 조회 */
    public StoreDetailResponse getStoreDetails(UUID storeUuid, UserEntity user) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 가게입니다."));

        // 로그인한 사용자인 경우에만 userId, userUuid 포함
        Long userId = (user != null) ? user.getId() : null;
        UUID userUuid = (user != null) ? user.getUserUuid() : null;

        // 가게 대표 이미지
        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);

        // 사장님 픽 이미지
        List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);

        // 메뉴 리스트
        List<MenuResponse> menus = menuService.getMenusByStore(storeUuid);

        // 한줄 리뷰
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.REVIEW,
                reviews.stream().map(StoreReview::getReviewId).toList());

        List<StoreReviewResponse> reviewResponses = reviews.stream()
                .map(review -> StoreReviewResponse.fromEntity(review, reviewImagesMap.getOrDefault(review.getReviewId(), List.of())))
                .toList();

        // 태그 조회
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeUuid);

        // 운영 시간
        List<StoreOperatingHour> operatingHours = storeOperatingHourRepository.findByStoreId(storeId);

        List<OperatingHourResponse> operatingHourResponses = operatingHours.stream()
                .map(o -> OperatingHourResponse.builder()
                        .dayOfWeek(o.getDayOfWeek())
                        .openingTime(o.getOpeningTime())
                        .closingTime(o.getClosingTime())
                        .lastOrderTime(o.getLastOrderTime())
                        .isClosed(o.getIsClosed())
                        .build())
                .toList();

        // 휴무일
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreId(storeId);

        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> HolidayResponse.builder()
                        .date(h.getHolidayDate().toString())
                        .reason(h.getReason())
                        .build())
                .toList();

        // 가게 상세 정보 반환
        return StoreDetailResponse.fromEntity(store, userId, userUuid, operatingHourResponses, holidayResponses, menus,
                storeImages, ownerPickImages, reviewResponses, tags);
    }

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
    @Transactional
    public void updateAverageRating(Long storeId) {
        BigDecimal newAverageRating = storeReviewRepository.findAverageRatingByStoreId(storeId);
        storeRepository.updateAverageRating(storeId, newAverageRating);
    }
}
