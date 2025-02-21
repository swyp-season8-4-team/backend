package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.response.MateResponse;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.entity.MateCategory;
import org.swyp.dessertbee.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
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
    private final MateCategoryRepository mateCategoryRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
            throw new BusinessException(ErrorCode.INVALID_TAG_SELECTION);
        }

        // 선택한 태그 조회
        List<StoreTag> selectedTags = storeTagRepository.findByIdIn(tagIds);

        // 태그가 유효한지 검증 (혹시 존재하지 않는 태그 ID가 포함되었는지 체크)
        if (selectedTags.size() != tagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_TAG_INCLUDED);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 로그인한 사용자인 경우에만 userId, userUuid 포함
        Long userId = (user != null) ? user.getId() : null;
        UUID userUuid = (user != null) ? user.getUserUuid() : null;

        // 가게 대표 이미지
        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);

        // 사장님 픽 이미지
        List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);

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

        // 해당 가게를 저장한 사람들의 취향 태그 Top3 조회
        List<Object[]> preferenceCounts = savedStoreRepository.findTop3PreferencesByStoreId(storeId);
        List<String> topPreferences = preferenceCounts.stream()
                .map(result -> (String) result[0])
                .toList();

        // 메뉴 리스트
        List<MenuResponse> menus = menuService.getMenusByStore(storeUuid);

        // 한줄 리뷰
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        int totalReviewCount = reviews.size();
        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.REVIEW,
                reviews.stream().map(StoreReview::getReviewId).toList());

        List<StoreReviewResponse> reviewResponses = reviews.stream().map(review -> {
            UserEntity reviewer = userRepository.findById(review.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

            return StoreReviewResponse.fromEntity(review, reviewer.getNickname(),
                    profileImage.isEmpty() ? null : profileImage.get(0),
                    reviewImagesMap.getOrDefault(review.getReviewId(), Collections.emptyList()));
        }).toList();

        // 디저트 메이트
        List<Mate> mates = mateRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        List<MateResponse> mateResponses = mates.stream().map(mate -> {
            UserEntity mateCreator = userRepository.findById(mate.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            String mateCategory = mateCategoryRepository.findById(mate.getMateCategoryId())
                    .map(MateCategory::getName).orElse("알 수 없음");
            List<String> mateThumbnail = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());
            int currentMembers = mateMemberRepository.countByMateIdAndApprovalYn(mate.getMateId(), true);

            return MateResponse.builder()
                    .mateUuid(mate.getMateUuid())
                    .mateCategory(mateCategory)
                    .thumbnail(mateThumbnail.isEmpty() ? null : mateThumbnail.get(0))
                    .title(mate.getTitle())
                    .content(mate.getContent())
                    .currentMembers(currentMembers)
                    .nickname(mateCreator.getNickname())
                    .recruitYn(mate.getRecruitYn())
                    .build();
        }).toList();

        return StoreDetailResponse.fromEntity(store, userId, userUuid, totalReviewCount, operatingHourResponses, holidayResponses, menus,
                storeImages, ownerPickImages, topPreferences, reviewResponses, tags, mateResponses);
    }

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
    @Transactional
    public void updateAverageRating(Long storeId) {
        BigDecimal newAverageRating = storeReviewRepository.findAverageRatingByStoreId(storeId);
        storeRepository.updateAverageRating(storeId, newAverageRating);
    }
}
