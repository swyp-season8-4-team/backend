package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.preference.exception.PreferenceExceptions.*;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.response.MateResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateCategory;
import org.swyp.dessertbee.community.mate.entity.SavedMate;
import org.swyp.dessertbee.community.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.community.mate.repository.SavedMateRepository;
import org.swyp.dessertbee.community.review.dto.response.ReviewSummaryResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewContent;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.*;
import org.swyp.dessertbee.store.store.entity.*;
import org.swyp.dessertbee.store.store.repository.*;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final MateRepository mateRepository;
    private final ImageService imageService;
    private final MenuService menuService;
    private final UserRepository userRepository;
    private final SavedMateRepository savedMateRepository;
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    public StoreDetailResponse createStore(StoreCreateRequest request,
                                           List<MultipartFile> storeImageFiles,
                                           List<MultipartFile> ownerPickImageFiles,
                                           List<MultipartFile> menuImageFiles) {
        try{
            Long ownerId = userRepository.findIdByUserUuid(request.getUserUuid());

            if (menuImageFiles == null) {
                menuImageFiles = Collections.emptyList(); // menuImageFiles가 null이면 빈 리스트로 처리
            }

            Store store = storeRepository.save(
                    Store.builder()
                            .ownerId(ownerId)
                            .ownerUuid(request.getUserUuid())
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

            return getStoreDetails(store.getStoreUuid());
        } catch (StoreCreationFailedException e){
            log.warn("가게 등록 실패 - 업주Uuid: {}, 사유: {}", request.getUserUuid(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 업주Uuid: {}", request.getUserUuid(), e);
            throw new StoreServiceException("가게 등록 처리 중 오류가 발생했습니다.");
        }
    }

    /** 태그 저장 (1~3개 선택) */
    private void saveStoreTags(Store store, List<Long> tagIds) {
        try{
            if (tagIds == null || tagIds.isEmpty() || tagIds.size() > 3) {
                log.warn("태그 선택 갯수 오류");
                throw new InvalidTagSelectionException("태그 선택 갯수가 잘못되었습니다.");
            }

            // 선택한 태그 조회
            List<StoreTag> selectedTags = storeTagRepository.findByIdIn(tagIds);

            // 태그가 유효한지 검증 (혹시 존재하지 않는 태그 ID가 포함되었는지 체크)
            if (selectedTags.size() != tagIds.size()) {
                log.warn("존재하지 않는 태그 오류");
                throw new InvalidTagIncludedException("유효하지 않은 태그값이 포함되었습니다.");
            }

            // 태그-가게 관계 저장
            List<StoreTagRelation> tagRelations = selectedTags.stream()
                    .map(tag -> StoreTagRelation.builder()
                            .store(store)
                            .tag(tag)
                            .build())
                    .collect(Collectors.toList());

            storeTagRelationRepository.saveAll(tagRelations);
        } catch (StoreTagSaveFailedException e){
            log.warn("태그 저장 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("태그 저장 처리 중 오류 발생", e);
            throw new StoreServiceException("태그 저장 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 가게 조회 */
    public List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius) {
        try{
            List<Store> stores = storeRepository.findStoresByLocation(lat, lng, radius);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .collect(Collectors.toList());
        } catch (StoreMapReadException e){
            log.warn("반경 내 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 특정 취향 태그를 가지는 가게 조회 */
    public List<StoreMapResponse> getStoresByLocationAndTags(Double lat, Double lng, Double radius, List<Long> preferenceTagIds) {
        try{
            if (preferenceTagIds == null || preferenceTagIds.isEmpty()) {
                throw new PreferencesNotFoundException("취향 태그가 선택되지 않았습니다.");
            }

            // 여러 태그 중 하나라도 매칭되는 가게를 조회
            List<Store> stores = storeRepository.findStoresByLocationAndTags(lat, lng, radius, preferenceTagIds);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .collect(Collectors.toList());
        } catch (StoreMapReadException e){
            log.warn("반경 내 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 가게 조회 및 검색 */
    public List<StoreMapResponse> getStoresByLocationAndKeyword(Double lat, Double lng, Double radius, String searchKeyword) {
        try{
            List<Store> stores = storeRepository.findStoresByLocationAndKeyword(lat, lng, radius, searchKeyword);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .collect(Collectors.toList());
        } catch (StoreSearchFailedException e){
            log.warn("반경 내 가게 검색 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 가게 검색 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 가게 검색 처리 중 오류가 발생했습니다.");
        }
    }

    /** 반경 내 사용자 취향 태그에 해당하는 가게 조회 */
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
                    .collect(Collectors.toList());

            if (preferenceTagIds.isEmpty()) {
                throw new UserPreferencesNotFoundException();
            }

            // 사용자의 취향 태그 중 하나라도 매칭되는 가게 조회
            List<Store> stores = storeRepository.findStoresByLocationAndTags(lng, lat, radius, preferenceTagIds);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .collect(Collectors.toList());
        } catch (PreferenceStoreReadException e){
            log.warn("반경 내 사용자 취향 맞춤 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** Store 엔티티를 StoreMapResponse DTO로 변환 */
    private StoreMapResponse convertToStoreMapResponse(Store store) {
        List<StoreOperatingHour> operatingHours = storeOperatingHourRepository.findByStoreId(store.getStoreId()); // 변환 없이 그대로 전달
        int totalReviewCount = storeReviewRepository.countByStoreIdAndDeletedAtIsNull(store.getStoreId());
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(store.getStoreId());
        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId());

        return StoreMapResponse.fromEntity(store, operatingHours, totalReviewCount, tags, storeImages);
    }

    /** 가게 간략 정보 조회 */
    public StoreSummaryResponse getStoreSummary(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
            List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);
            List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

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
        } catch (StoreInfoReadFailedException e){
            log.warn("가게 간략정보 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 간략정보 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 간략정보 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 상세 정보 조회 */
    public StoreDetailResponse getStoreDetails(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            UserEntity user = userService.getCurrentUser();
            Long userId = (user != null) ? user.getId() : null;
            UUID userUuid = (user != null) ? user.getUserUuid() : null;

            Optional<SavedStore> savedStoreOpt = (userId != null) ?
                    savedStoreRepository.findFirstByStoreAndUserId(store, userId) :
                    Optional.empty();

            boolean saved = savedStoreOpt.isPresent();
            Long savedListId = savedStoreOpt.map(s -> s.getUserStoreList().getId()).orElse(null);

            log.info("사용자가 가게를 저장했는지 여부: {}, savedListId: {}", saved, savedListId);

            // 가게 대표 이미지
            List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);

            // 사장님 픽 이미지
            List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);

            // 태그 조회
            List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

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
            Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.SHORT,
                    reviews.stream().map(StoreReview::getReviewId).toList());

            List<StoreReviewResponse> reviewResponses = reviews.stream().map(review -> {
                Long reviewerId = userRepository.findIdByUserUuid(review.getUserUuid());
                UserEntity reviewer = userRepository.findById(reviewerId)
                        .orElseThrow(() -> new UserNotFoundException());
                List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

                return StoreReviewResponse.fromEntity(review, reviewer,
                        profileImage.isEmpty() ? null : profileImage.get(0),
                        reviewImagesMap.getOrDefault(review.getReviewId(), Collections.emptyList()));
            }).toList();

            // 가게의 리뷰 목록 가져오기
            List<Review> storeReviews = reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

            // 커뮤니티 리뷰 응답 DTO 변환
            List<ReviewSummaryResponse> communityResponses = storeReviews.stream().map(review -> {
                // 리뷰 작성자 정보 조회
                UserEntity reviewer = userRepository.findById(review.getUserId())
                        .orElseThrow(() -> new UserNotFoundException());

                List<String> profileImageList = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());
                String profileImage = profileImageList.isEmpty() ? null : profileImageList.get(0);

                String thumbnail = null;
                String content = "";

                for (ReviewContent contentItem : review.getReviewContents()) {
                    if (thumbnail == null && "image".equals(contentItem.getType())) {
                        thumbnail = contentItem.getValue();
                    } else if (content.isEmpty() && "text".equals(contentItem.getType())) {
                        content = contentItem.getValue();
                    }

                    // 두 값이 모두 설정되면 반복문 끝
                    if (thumbnail != null && !content.isEmpty()) {
                        break;
                    }
                }

                return ReviewSummaryResponse.builder()
                        .reviewUuid(review.getReviewUuid())
                        .userUuid(reviewer.getUserUuid())
                        .nickname(reviewer.getNickname())
                        .profileImage(profileImage)
                        .thumbnail(thumbnail)
                        .title(review.getTitle())
                        .content(content)
                        .createdAt(review.getCreatedAt())
                        .updatedAt(review.getUpdatedAt())
                        .build();
            }).collect(Collectors.toList());

            // 디저트 메이트
            List<Mate> mates = mateRepository.findByStoreIdAndDeletedAtIsNull(storeId);
            List<MateResponse> mateResponses = mates.stream().map(mate -> {
                UserEntity mateCreator = userRepository.findById(mate.getUserId())
                        .orElseThrow(() -> new UserNotFoundException());
                String mateCategory = mateCategoryRepository.findById(mate.getMateCategoryId())
                        .map(MateCategory::getName).orElse("알 수 없음");
                List<String> mateThumbnail = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());

                //저장했는지 유무 확인
                SavedMate savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
                boolean mateSaved = (savedMate != null);

                return MateResponse.builder()
                        .mateUuid(mate.getMateUuid())
                        .mateCategory(mateCategory)
                        .thumbnail(mateThumbnail.isEmpty() ? null : mateThumbnail.get(0))
                        .title(mate.getTitle())
                        .content(mate.getContent())
                        .nickname(mateCreator.getNickname())
                        .recruitYn(mate.getRecruitYn())
                        .saved(mateSaved)
                        .build();
            }).toList();

            // 조회수 증가
            StoreStatistics statistics = storeStatisticsRepository.findByStoreId(storeId)
                    .orElseThrow(() -> new StoreInfoReadFailedException("통계 정보가 존재하지 않습니다."));
            statistics.increaseViews();
            storeStatisticsRepository.save(statistics);

            return StoreDetailResponse.fromEntity(store, userId, userUuid, totalReviewCount, operatingHourResponses, holidayResponses, menus,
                    storeImages, ownerPickImages, topPreferences, reviewResponses, tags, communityResponses, mateResponses, saved, savedListId);
        } catch (StoreInfoReadFailedException e){
            log.warn("가게 상세정보 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 상세정보 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 상세정보 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
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

    /** 가게 수정 */
    public StoreDetailResponse updateStore(UUID storeUuid,
                                           StoreUpdateRequest request,
                                           List<MultipartFile> storeImageFiles,
                                           List<MultipartFile> ownerPickImageFiles,
                                           List<MultipartFile> menuImageFiles) {
        try{
            // 가게 존재 여부 및 삭제 여부 체크
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            if (!store.getOwnerUuid().equals(request.getUserUuid())) {
                throw new UnauthorizedAccessException();
            }

            // 가게 기본 정보 업데이트
            store.updateInfo(
                    request.getName(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getStoreLink(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getDescription(),
                    Boolean.TRUE.equals(request.getAnimalYn()),
                    Boolean.TRUE.equals(request.getTumblerYn()),
                    Boolean.TRUE.equals(request.getParkingYn()),
                    request.getNotice()
            );
            storeRepository.save(store);

            if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
                List<Long> deleteStoreImageIds = request.getStoreImageDeleteIds();
                String folder = "store/" + store.getStoreId();
                imageService.updatePartialImages(deleteStoreImageIds, storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
            }

            if (ownerPickImageFiles != null && !ownerPickImageFiles.isEmpty()) {
                List<Long> deleteOwnerPickImageIds = request.getOwnerPickImageDeleteIds();
                String folder = "ownerpick/" + store.getStoreId();
                imageService.updatePartialImages(deleteOwnerPickImageIds, ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
            }

            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                storeTagRelationRepository.deleteByStore(store);
                saveStoreTags(store, request.getTagIds());
            }

            if (request.getMenus() != null && !request.getMenus().isEmpty()) {
                Map<String, MultipartFile> menuImageMap = menuImageFiles.stream()
                        .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> b));

                for (StoreUpdateRequest.MenuRequest menuRequest : request.getMenus()) {
                    MultipartFile file = null;
                    if (menuRequest.getImageFileKey() != null) {
                        file = menuImageMap.get(menuRequest.getImageFileKey());
                    }

                    MenuCreateRequest menuCreateRequest = convertToMenuCreateRequest(menuRequest);

                    if (menuRequest.getMenuUuid() != null) {
                        menuService.updateMenu(store.getStoreUuid(), menuRequest.getMenuUuid(), menuCreateRequest, file);
                    } else {
                        menuService.addMenu(store.getStoreUuid(), menuCreateRequest, file);
                    }
                }
            }

            if (request.getOperatingHours() != null) {
                storeOperatingHourRepository.deleteByStoreId(store.getStoreId());
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

            if (request.getHolidays() != null) {
                storeHolidayRepository.deleteByStoreId(store.getStoreId());
                List<StoreHoliday> holidays = request.getHolidays().stream()
                        .map(holiday -> StoreHoliday.builder()
                                .storeId(store.getStoreId())
                                .holidayDate(LocalDate.parse(holiday.getDate()))
                                .reason(holiday.getReason())
                                .build())
                        .collect(Collectors.toList());
                storeHolidayRepository.saveAll(holidays);
            }

            return getStoreDetails(store.getStoreUuid());
        } catch (StoreUpdateException e){
            log.warn("가게 수정 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 수정 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 수정 처리 중 오류가 발생했습니다.");
        }
    }

    private MenuCreateRequest convertToMenuCreateRequest(StoreUpdateRequest.MenuRequest menuRequest) {
        MenuCreateRequest mcr = new MenuCreateRequest();
        mcr.setMenuUuid(menuRequest.getMenuUuid());
        mcr.setName(menuRequest.getName());
        mcr.setPrice(menuRequest.getPrice());
        mcr.setIsPopular(menuRequest.getIsPopular());
        mcr.setDescription(menuRequest.getDescription());
        mcr.setImageFileKey(menuRequest.getImageFileKey());
        return mcr;
    }

    /** 가게 삭제 */
    public void deleteStore(UUID storeUuid) {
        try{
            UserEntity user = userService.getCurrentUser();
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            if (!store.getOwnerUuid().equals(user.getUserUuid())) {
                throw new UnauthorizedAccessException();
            }

            store.softDelete();
            storeRepository.save(store);
        } catch (StoreDeleteException e){
            log.warn("가게 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 삭제 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 삭제 처리 중 오류가 발생했습니다.");
        }
    }
}
