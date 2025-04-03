package org.swyp.dessertbee.store.store.service;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.preference.exception.PreferenceExceptions.*;
import org.swyp.dessertbee.store.menu.converter.MenuConverter;
import org.swyp.dessertbee.store.menu.entity.Menu;
import org.swyp.dessertbee.store.menu.repository.MenuRepository;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.store.dto.request.BaseStoreRequest;
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
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
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

/**
 * StoreService 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {

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
    private final StoreLinkRepository storeLinkRepository;
    private final StoreBreakTimeRepository storeBreakTimeRepository;
    private final MenuRepository menuRepository;

    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    @Override
    public void createStore(StoreCreateRequest request,
                                           List<MultipartFile> storeImageFiles,
                                           List<MultipartFile> ownerPickImageFiles,
                                           List<MultipartFile> menuImageFiles) {
        try {
            // 점주 ID 조회
            Long ownerId = userRepository.findIdByUserUuid(request.getUserUuid());

            // 가게 저장
            Store store = storeRepository.save(
                    Store.builder()
                            .ownerId(ownerId)
                            .ownerUuid(request.getUserUuid())
                            .name(request.getName())
                            .phone(request.getPhone())
                            .address(request.getAddress())
                            .latitude(request.getLatitude())
                            .longitude(request.getLongitude())
                            .description(request.getDescription())
                            .animalYn(Boolean.TRUE.equals(request.getAnimalYn()))
                            .tumblerYn(Boolean.TRUE.equals(request.getTumblerYn()))
                            .parkingYn(Boolean.TRUE.equals(request.getParkingYn()))
                            .build()
            );

            // 가게 링크 저장
            validateAndSaveStoreLinks(store, request.getStoreLinks());

            // 가게 통계 초기화
            storeStatisticsRepository.save(
                    StoreStatistics.builder()
                            .storeId(store.getStoreId())
                            .views(0)
                            .saves(0)
                            .reviews(0)
                            .createDate(LocalDate.now())  // 가게가 등록된 날짜
                            .build()
            );

            // 이미지 처리
            processStoreImages(store, storeImageFiles, null);
            processOwnerPickImages(store, ownerPickImageFiles, null);

            // 태그 저장
            saveStoreTags(store, request.getTagIds());

            // 메뉴 저장
            processMenus(store, request.getMenus(), menuImageFiles, false);

            // 영업 시간 저장
            saveOrUpdateOperatingHours(store, request.getOperatingHours());

            // 휴무일 저장
            //saveOrUpdateHolidays(store, request.getHolidays());
        } catch (StoreCreationFailedException e) {
            log.warn("가게 등록 실패 - 업주Uuid: {}, 사유: {}", request.getUserUuid(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 등록 처리 중 오류 발생 - 업주Uuid: {}", request.getUserUuid(), e);
            throw new StoreServiceException("가게 등록 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 영업 시간 저장/갱신 메서드
     */
    private void saveOrUpdateOperatingHours(Store store, List<BaseStoreRequest.OperatingHourRequest> operatingHoursRequest) {
        if (operatingHoursRequest != null) {
            // 기존 영업 시간 데이터 삭제
            storeOperatingHourRepository.deleteByStoreId(store.getStoreId());

            // 기존 휴게 시간 데이터 삭제
            storeBreakTimeRepository.deleteByOperatingHourIdIn(
                    storeOperatingHourRepository.findIdsByStoreId(store.getStoreId())
            );

            // 영업 시간 데이터 저장
            List<StoreOperatingHour> operatingHours = operatingHoursRequest.stream()
                    .map(hour -> StoreOperatingHour.builder()
                            .storeId(store.getStoreId())
                            .dayOfWeek(hour.getDayOfWeek())
                            .openingTime(hour.getOpeningTime())
                            .closingTime(hour.getClosingTime())
                            .lastOrderTime(hour.getLastOrderTime())
                            .isClosed(hour.getIsClosed())
                            .regularClosureType(hour.getRegularClosureType())
                            .regularClosureWeeks(hour.getRegularClosureWeeks())
                            .build())
                    .toList();

            List<StoreOperatingHour> savedOperatingHours = storeOperatingHourRepository.saveAll(operatingHours);

            // 휴게 시간 데이터 저장
            List<StoreBreakTime> breakTimes = new ArrayList<>();
            for (int i = 0; i < operatingHoursRequest.size(); i++) {
                BaseStoreRequest.OperatingHourRequest hourRequest = operatingHoursRequest.get(i);
                StoreOperatingHour savedHour = savedOperatingHours.get(i);

                if (hourRequest.getBreakTimes() != null && !hourRequest.getBreakTimes().isEmpty()) {
                    List<StoreBreakTime> dayBreakTimes = hourRequest.getBreakTimes().stream()
                            .map(breakTime -> StoreBreakTime.builder()
                                    .operatingHourId(savedHour.getId())
                                    .startTime(breakTime.getStartTime())
                                    .endTime(breakTime.getEndTime())
                                    .build())
                            .toList();
                    breakTimes.addAll(dayBreakTimes);
                }
            }

            if (!breakTimes.isEmpty()) {
                storeBreakTimeRepository.saveAll(breakTimes);
            }
        }
    }

    /**
     * 휴무일 저장/갱신 메서드
     */
    private void saveOrUpdateHolidays(Store store, List<BaseStoreRequest.HolidayRequest> holidaysRequest) {
        if (holidaysRequest != null) {
            storeHolidayRepository.deleteByStoreId(store.getStoreId());
            List<StoreHoliday> holidays = holidaysRequest.stream()
                    .map(holiday -> StoreHoliday.builder()
                            .storeId(store.getStoreId())
                            .holidayDate(LocalDate.parse(holiday.getDate()))
                            .reason(holiday.getReason())
                            .build())
                    .toList();
            storeHolidayRepository.saveAll(holidays);
        }
    }

    /**
     * 링크 유효성 검사 및 저장 메서드
     * List<? extends StoreLinkRequest> 타입으로 변경하여 제네릭 타입 호환성 문제 해결
     */
    private void validateAndSaveStoreLinks(Store store, List<? extends BaseStoreRequest.StoreLinkRequest> linkRequests) {
        if (linkRequests != null && !linkRequests.isEmpty()) {
            // 기본 링크 중복 체크
            long primaryCount = linkRequests.stream()
                    .filter(link -> Boolean.TRUE.equals(link.getIsPrimary()))
                    .count();

            if (primaryCount > 1) {
                throw new DuplicatePrimaryLinkException();
            }

            List<StoreLink> links = linkRequests.stream()
                    .map(linkReq -> StoreLink.builder()
                            .storeId(store.getStoreId())
                            .url(linkReq.getUrl())
                            .isPrimary(Boolean.TRUE.equals(linkReq.getIsPrimary()))
                            .build())
                    .toList();

            storeLinkRepository.saveAll(links);
        }
    }

    /**
     * 가게 이미지 처리 메서드
     */
    private void processStoreImages(Store store, List<MultipartFile> storeImageFiles, List<Long> deleteImageIds) {
        if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
            String folder = "store/" + store.getStoreId();
            if (deleteImageIds != null) {
                // 업데이트 시 호출
                imageService.updatePartialImages(deleteImageIds, storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
            } else {
                // 생성 시 호출
                imageService.uploadAndSaveImages(storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
            }
        }
    }

    /**
     * 사장님 픽 이미지 처리 메서드
     */
    private void processOwnerPickImages(Store store, List<MultipartFile> ownerPickImageFiles, List<Long> deleteImageIds) {
        if (ownerPickImageFiles != null && !ownerPickImageFiles.isEmpty()) {
            String folder = "ownerpick/" + store.getStoreId();
            if (deleteImageIds != null) {
                // 업데이트 시 호출
                imageService.updatePartialImages(deleteImageIds, ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
            } else {
                // 생성 시 호출
                imageService.uploadAndSaveImages(ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
            }
        }
    }

    /**
     * 메뉴 이미지 맵 생성 유틸리티 메서드
     */
    private Map<String, MultipartFile> createMenuImageMap(List<MultipartFile> menuImageFiles) {
        if (menuImageFiles == null) {
            return Collections.emptyMap();
        }
        return menuImageFiles.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> b));
    }

    /**
     * 메뉴 처리 메서드
     */
    private <T> void processMenus(Store store, List<T> menuRequests, List<MultipartFile> menuImageFiles, boolean isUpdate) {
        if (menuRequests != null && !menuRequests.isEmpty()) {
            Map<String, MultipartFile> menuImageMap = createMenuImageMap(menuImageFiles);

            if (isUpdate) {
                // 업데이트 시 처리 로직
                if (menuRequests.get(0) instanceof StoreUpdateRequest.MenuRequest) {
                    @SuppressWarnings("unchecked")
                    List<StoreUpdateRequest.MenuRequest> typedRequests = (List<StoreUpdateRequest.MenuRequest>) menuRequests;

                    for (StoreUpdateRequest.MenuRequest menuRequest : typedRequests) {
                        MultipartFile file = null;
                        if (menuRequest.getImageFileKey() != null) {
                            file = menuImageMap.get(menuRequest.getImageFileKey());
                        }

                        MenuCreateRequest menuCreateRequest = MenuConverter.convertToMenuCreateRequest(menuRequest);

                        if (menuRequest.getMenuUuid() != null) {
                            menuService.updateMenu(store.getStoreUuid(), menuRequest.getMenuUuid(), menuCreateRequest, file);
                        } else {
                            menuService.addMenu(store.getStoreUuid(), menuCreateRequest, file);
                        }
                    }
                }
            } else {
                // 생성 시 처리 로직
                if (menuRequests.get(0) instanceof MenuCreateRequest) {
                    @SuppressWarnings("unchecked")
                    List<MenuCreateRequest> typedRequests = (List<MenuCreateRequest>) menuRequests;
                    menuService.addMenus(store.getStoreUuid(), typedRequests, menuImageMap);
                }
            }
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
                    .toList();

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
    @Override
    public List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius) {
        try{
            List<Store> stores = storeRepository.findStoresByLocation(lat, lng, radius);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .toList();
        } catch (StoreMapReadException e){
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

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .toList();
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
            List<Store> stores = storeRepository.findStoresByLocationAndKeyword(lat, lng, radius, searchKeyword);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .toList();
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
            List<Store> stores = storeRepository.findStoresByLocationAndTags(lng, lat, radius, preferenceTagIds);

            return stores.stream()
                    .map(this::convertToStoreMapResponse)
                    .toList();
        } catch (PreferenceStoreReadException e){
            log.warn("반경 내 사용자 취향 맞춤 가게 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류 발생", e);
            throw new StoreServiceException("반경 내 사용자 취향 맞춤 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 가게 링크 조회 및 대표 링크 추출 메서드
     */
    private Pair<List<String>, String> getStoreLinksAndPrimary(Long storeId) {
        List<StoreLink> storeLinks = storeLinkRepository.findByStoreId(storeId);

        String primaryStoreLink = storeLinks.stream()
                .filter(StoreLink::getIsPrimary)
                .map(StoreLink::getUrl)
                .findFirst()
                .orElse(null);

        List<String> linkUrls = storeLinks.stream()
                .map(StoreLink::getUrl)
                .toList();

        return Pair.of(linkUrls, primaryStoreLink);
    }

    /**
     * 운영 시간 조회 및 변환 메서드
     */
    private List<OperatingHourResponse> getOperatingHoursResponse(Long storeId) {
        List<StoreOperatingHour> operatingHours = storeOperatingHourRepository.findByStoreId(storeId);

        return operatingHours.stream()
                .map(o -> {
                    // 휴게시간 조회
                    List<BreakTimeResponse> breakTimes = storeBreakTimeRepository.findByOperatingHourId(o.getId())
                            .stream()
                            .map(b -> BreakTimeResponse.builder()
                                    .startTime(b.getStartTime())
                                    .endTime(b.getEndTime())
                                    .build())
                            .toList();

                    return OperatingHourResponse.builder()
                            .dayOfWeek(o.getDayOfWeek())
                            .openingTime(o.getOpeningTime())
                            .closingTime(o.getClosingTime())
                            .lastOrderTime(o.getLastOrderTime())
                            .isClosed(o.getIsClosed())
                            .regularClosureType(o.getRegularClosureType() != null ? o.getRegularClosureType().name() : null)
                            .regularClosureWeeks(o.getRegularClosureWeeks())
                            .breakTimes(breakTimes)
                            .build();
                })
                .toList();
    }

    /**
     * 휴무일 조회 및 변환 메서드
     */
    private List<HolidayResponse> getHolidaysResponse(Long storeId) {
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreId(storeId);

        return holidays.stream()
                .map(h -> HolidayResponse.builder()
                        .date(h.getHolidayDate().toString())
                        .reason(h.getReason())
                        .build())
                .toList();
    }

    /**
     * 가게의 Top3 취향 태그 조회 메서드
     */
    private List<String> getTop3Preferences(Long storeId) {
        List<Object[]> preferenceCounts = savedStoreRepository.findTop3PreferencesByStoreId(storeId);

        return preferenceCounts.stream()
                .map(result -> (String) result[0])
                .toList();
    }

    /**
     * 사용자의 가게 저장 정보 조회 메서드
     */
    private Pair<Boolean, Long> getUserStoreSavedInfo(Store store, UserEntity user) {
        if (user == null) {
            return Pair.of(false, null);
        }

        Optional<SavedStore> savedStoreOpt = savedStoreRepository.findFirstByStoreAndUserId(store, user.getId());
        boolean saved = savedStoreOpt.isPresent();
        Long savedListId = savedStoreOpt.map(s -> s.getUserStoreList().getId()).orElse(null);

        log.info("사용자가 가게를 저장했는지 여부: {}, savedListId: {}", saved, savedListId);
        return Pair.of(saved, savedListId);
    }

    /**
     * Store 엔티티를 StoreMapResponse DTO로 변환
     */
    private StoreMapResponse convertToStoreMapResponse(Store store) {
        List<OperatingHourResponse> operatingHours = getOperatingHoursResponse(store.getStoreId());
        List<HolidayResponse> holidays = getHolidaysResponse(store.getStoreId());
        int totalReviewCount = storeReviewRepository.countByStoreIdAndDeletedAtIsNull(store.getStoreId());
        List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(store.getStoreId());
        List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId());

        return StoreMapResponse.fromEntity(store, operatingHours, holidays, totalReviewCount, tags, storeImages);
    }

    /**
     * 가게 한줄 리뷰 조회 및 변환 메서드
     */
    private List<StoreReviewResponse> getStoreReviewResponses(Long storeId) {
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.SHORT,
                reviews.stream().map(StoreReview::getReviewId).toList());

        return reviews.stream().map(review -> {
            Long reviewerId = userRepository.findIdByUserUuid(review.getUserUuid());
            UserEntity reviewer = userRepository.findById(reviewerId)
                    .orElseThrow(() -> new UserNotFoundException());
            List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

            return StoreReviewResponse.fromEntity(review, reviewer,
                    profileImage.isEmpty() ? null : profileImage.get(0),
                    reviewImagesMap.getOrDefault(review.getReviewId(), Collections.emptyList()));
        }).toList();
    }

    /**
     * 가게 커뮤니티 리뷰 조회 및 변환 메서드
     */
    private List<ReviewSummaryResponse> getCommunityReviewResponses(Long storeId) {
        List<Review> storeReviews = reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        return storeReviews.stream().map(review -> {
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
        }).toList();
    }

    /**
     * 디저트 메이트 조회 및 변환 메서드
     */
    private List<MateResponse> getMateResponses(Long storeId, Long userId) {
        List<Mate> mates = mateRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        return mates.stream().map(mate -> {
            UserEntity mateCreator = userRepository.findById(mate.getUserId())
                    .orElseThrow(() -> new UserNotFoundException());
            String mateCategory = mateCategoryRepository.findById(mate.getMateCategoryId())
                    .map(MateCategory::getName).orElse("알 수 없음");
            List<String> mateThumbnail = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());

            //저장했는지 유무 확인
            SavedMate savedMate = userId != null ?
                    savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId) : null;
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
    }

    /**
     * 가게 조회수 증가 메서드
     */
    private void increaseStoreViews(Long storeId) {
        StoreStatistics statistics = storeStatisticsRepository.findTopByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId)
                .orElseThrow(() -> new StoreInfoReadFailedException("통계 정보가 존재하지 않습니다."));
        statistics.increaseViews();
        storeStatisticsRepository.save(statistics);
    }

    /** 가게 간략 정보 조회 */
    @Override
    public StoreSummaryResponse getStoreSummary(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
            List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);
            List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

            // 가게 링크 및 대표 링크 조회
            Pair<List<String>, String> linkInfo = getStoreLinksAndPrimary(storeId);

            // 운영 시간 조회
            List<OperatingHourResponse> operatingHourResponses = getOperatingHoursResponse(storeId);

            // 휴무일 조회
            List<HolidayResponse> holidayResponses = getHolidaysResponse(storeId);

            // 가게 취향 태그 top3 조회
            List<String> topPreferences = getTop3Preferences(storeId);

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
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            // 사용자 정보 조회
            UserEntity user = userService.getCurrentUser();
            Long userId = (user != null) ? user.getId() : null;
            UUID userUuid = (user != null) ? user.getUserUuid() : null;

            // 사용자의 가게 저장 정보 조회
            Pair<Boolean, Long> savedInfo = getUserStoreSavedInfo(store, user);
            boolean saved = savedInfo.getLeft();
            Long savedListId = savedInfo.getRight();

            // 가게 이미지 조회
            List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
            List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);

            // 태그 조회
            List<String> tags = storeTagRelationRepository.findTagNamesByStoreId(storeId);

            // 가게 링크 및 대표 링크 조회
            Pair<List<String>, String> linkInfo = getStoreLinksAndPrimary(storeId);

            // 운영 시간 조회
            List<OperatingHourResponse> operatingHourResponses = getOperatingHoursResponse(storeId);

            // 휴무일 조회
            List<HolidayResponse> holidayResponses = getHolidaysResponse(storeId);

            // 가게 취향 태그 top3 조회
            List<String> topPreferences = getTop3Preferences(storeId);

            // 메뉴 리스트 조회
            List<MenuResponse> menus = menuService.getMenusByStore(storeUuid);

            // 한줄 리뷰 조회
            List<StoreReviewResponse> reviewResponses = getStoreReviewResponses(storeId);
            int totalReviewCount = reviewResponses.size();

            // 커뮤니티 리뷰 조회
            List<ReviewSummaryResponse> communityResponses = getCommunityReviewResponses(storeId);

            // 디저트 메이트 조회
            List<MateResponse> mateResponses = getMateResponses(storeId, userId);

            // 조회수 증가
            increaseStoreViews(storeId);

            return StoreDetailResponse.fromEntity(
                    store,
                    userId,
                    userUuid,
                    totalReviewCount,
                    operatingHourResponses,
                    holidayResponses,
                    menus,
                    storeImages,
                    ownerPickImages,
                    topPreferences,
                    reviewResponses,
                    tags,
                    linkInfo.getLeft(),
                    linkInfo.getRight(),
                    communityResponses,
                    mateResponses,
                    saved,
                    savedListId
            );
        } catch (StoreInfoReadFailedException e){
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

    /** 가게 수정 */
    @Override
    public void updateStore(UUID storeUuid,
                                           StoreUpdateRequest request,
                                           List<MultipartFile> storeImageFiles,
                                           List<MultipartFile> ownerPickImageFiles,
                                           List<MultipartFile> menuImageFiles) {
        try {
            // 가게 존재 여부 및 삭제 여부 체크
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            // 가게 소유자 체크
            if (!store.getOwnerUuid().equals(request.getUserUuid())) {
                throw new UnauthorizedAccessException();
            }

            // 가게 기본 정보 업데이트
            store.updateInfo(
                    request.getName(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getDescription(),
                    Boolean.TRUE.equals(request.getAnimalYn()),
                    Boolean.TRUE.equals(request.getTumblerYn()),
                    Boolean.TRUE.equals(request.getParkingYn())
            );
            storeRepository.save(store);

            // 가게 링크 저장 (기존 링크는 삭제 후 새로 저장)
            storeLinkRepository.deleteByStoreId(storeId);
            validateAndSaveStoreLinks(store, request.getStoreLinks());

            // 이미지 처리
            processStoreImages(store, storeImageFiles, request.getStoreImageDeleteIds());
            processOwnerPickImages(store, ownerPickImageFiles, request.getOwnerPickImageDeleteIds());

            // 태그 저장 (기존 태그는 삭제 후 새로 저장)
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                storeTagRelationRepository.deleteByStore(store);
                saveStoreTags(store, request.getTagIds());
            }

            // 메뉴 처리
            processMenus(store, request.getMenus(), menuImageFiles, true);

            // 영업 시간 저장
            saveOrUpdateOperatingHours(store, request.getOperatingHours());

            // 휴무일 저장
            //saveOrUpdateHolidays(store, request.getHolidays());
        } catch (StoreUpdateException e) {
            log.warn("가게 수정 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 수정 처리 중 오류 발생", e);
            throw new StoreServiceException("가게 수정 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 삭제 */
    @Override
    public void deleteStore(UUID storeUuid) {
        try{
            UserEntity user = userService.getCurrentUser();
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(StoreNotFoundException::new);

            if (!store.getOwnerUuid().equals(user.getUserUuid())) {
                throw new UnauthorizedAccessException();
            }

            // 관련 엔티티 삭제
            deleteRelatedData(storeId);

            imageService.deleteImagesByRefId(ImageType.OWNERPICK, storeId);
            imageService.deleteImagesByRefId(ImageType.STORE, storeId);

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

    private void deleteRelatedData(Long storeId) {
        // 1. SavedStore 삭제
        savedStoreRepository.deleteByStoreId(storeId);

        // 2. StoreOperatingHour → StoreBreakTime → 삭제
        storeBreakTimeRepository.deleteAllByStoreId(storeId);
        storeOperatingHourRepository.deleteByStoreId(storeId);

        // 3. StoreHoliday 삭제
        storeHolidayRepository.deleteByStoreId(storeId);

        // 4. StoreLink 삭제
        storeLinkRepository.deleteByStoreId(storeId);

        // 5. StoreStatistics soft delete
        List<StoreStatistics> stats = storeStatisticsRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);
        for (StoreStatistics stat : stats) {
            stat.softDelete();
        }
        storeStatisticsRepository.saveAll(stats);

        // 6. StoreTagRelation 삭제
        storeTagRelationRepository.deleteByStoreId(storeId);

        // 7. StoreReview soft delete
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        for (StoreReview review : reviews) {
            review.softDelete();
        }
        storeReviewRepository.saveAll(reviews);

        // 8. Menu soft delete
        List<Menu> menus = menuRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        for (Menu menu : menus) {
            menu.softDelete();
        }
        menuRepository.saveAll(menus);
    }
}
