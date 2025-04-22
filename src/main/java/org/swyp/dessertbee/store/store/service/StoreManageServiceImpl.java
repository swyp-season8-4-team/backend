package org.swyp.dessertbee.store.store.service;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.search.service.StoreSearchService;
import org.swyp.dessertbee.statistics.store.entity.StoreStatistics;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsRepository;
import org.swyp.dessertbee.store.link.repository.StoreLinkRepository;
import org.swyp.dessertbee.store.link.service.StoreLinkService;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.entity.Menu;
import org.swyp.dessertbee.store.menu.repository.MenuRepository;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.notice.dto.response.StoreNoticeResponse;
import org.swyp.dessertbee.store.notice.entity.StoreNotice;
import org.swyp.dessertbee.store.notice.repository.StoreNoticeRepository;
import org.swyp.dessertbee.store.notice.service.StoreNoticeService;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.saved.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.schedule.repository.StoreBreakTimeRepository;
import org.swyp.dessertbee.store.schedule.repository.StoreHolidayRepository;
import org.swyp.dessertbee.store.schedule.repository.StoreOperatingHourRepository;
import org.swyp.dessertbee.store.schedule.service.StoreScheduleService;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.schedule.dto.HolidayResponse;
import org.swyp.dessertbee.store.schedule.dto.OperatingHourResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreInfoResponse;
import org.swyp.dessertbee.store.store.handler.StoreImageHandler;
import org.swyp.dessertbee.store.store.handler.StoreMenuHandler;
import org.swyp.dessertbee.store.tag.dto.StoreTagResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.schedule.entity.StoreHoliday;
import org.swyp.dessertbee.store.store.exception.StoreExceptions;
import org.swyp.dessertbee.store.store.repository.*;
import org.swyp.dessertbee.store.tag.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.tag.service.StoreTagService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreManageServiceImpl implements StoreManageService{

    private final StoreRepository storeRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final StoreStatisticsRepository storeStatisticsRepository;
    private final StoreOperatingHourRepository storeOperatingHourRepository;
    private final StoreHolidayRepository storeHolidayRepository;
    private final SavedStoreRepository savedStoreRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final StoreLinkRepository storeLinkRepository;
    private final StoreNoticeRepository storeNoticeRepository;
    private final StoreBreakTimeRepository storeBreakTimeRepository;
    private final MenuRepository menuRepository;
    private final StoreSearchService storeSearchService;
    private final StoreLinkService storeLinkService;
    private final StoreImageHandler storeImageHandler;
    private final StoreMenuHandler storeMenuHandler;
    private final StoreTagService storeTagService;
    private final StoreScheduleService storeScheduleService;
    private final StoreNoticeService storeNoticeService;
    private final MenuService menuService;


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
            storeLinkService.validateAndSaveStoreLinks(store, request.getStoreLinks());

            // 새로운 가게 통계 저장
            storeStatisticsRepository.save(
                    StoreStatistics.builder()
                            .storeId(store.getStoreId())
                            .statDate(LocalDate.now())
                            .averageRating(BigDecimal.ZERO)
                            .build()
            );

            // 이미지 처리
            storeImageHandler.updateStoreImages(store, storeImageFiles, null);
            storeImageHandler.updateOwnerPickImages(store, ownerPickImageFiles, null);

            // 태그 저장
            storeTagService.saveStoreTags(store, request.getTagIds());

            // 메뉴 저장
            storeMenuHandler.processMenus(store, request.getMenus(), menuImageFiles);

            // 영업 시간 저장
            storeScheduleService.saveOrUpdateOperatingHours(store, request.getOperatingHours());

            // 휴무일 저장
            List<StoreHoliday> holidays = storeScheduleService.saveHolidays(request.getHolidays(), store.getStoreId());
            storeHolidayRepository.saveAll(holidays);

            storeSearchService.indexStore(store.getStoreId());
        } catch (StoreExceptions.StoreCreationFailedException e) {
            log.warn("가게 등록 실패 - 업주Uuid: {}, 사유: {}", request.getUserUuid(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 등록 처리 중 오류 발생 - 업주Uuid: {}", request.getUserUuid(), e);
            throw new StoreExceptions.StoreServiceException("가게 등록 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 수정 */
    @Override
    public StoreInfoResponse updateStore(UUID storeUuid,
                                         StoreUpdateRequest request,
                                         List<MultipartFile> storeImageFiles,
                                         List<MultipartFile> ownerPickImageFiles) {
        try {
            // 가게 존재 여부 및 삭제 여부 체크
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreExceptions.StoreNotFoundException());

            // 가게 소유자 체크
            if (!store.getOwnerUuid().equals(request.getUserUuid())) {
                throw new UserExceptions.UnauthorizedAccessException();
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
            storeLinkService.validateAndSaveStoreLinks(store, request.getStoreLinks());

            // 이미지 처리
            storeImageHandler.updateStoreImages(store, storeImageFiles, request.getStoreImageDeleteIds());
            storeImageHandler.updateOwnerPickImages(store, ownerPickImageFiles, request.getOwnerPickImageDeleteIds());

            // 태그 저장 (기존 태그는 삭제 후 새로 저장)
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                storeTagRelationRepository.deleteByStore(store);
                storeTagService.saveStoreTags(store, request.getTagIds());
            }

            // 영업 시간 저장
            storeScheduleService.saveOrUpdateOperatingHours(store, request.getOperatingHours());

            // 휴무일 저장
            storeScheduleService.saveHolidays(request.getHolidays(), storeId);

            storeSearchService.indexStore(storeId);

            return getStoreInfo(storeUuid);
        } catch (StoreExceptions.StoreUpdateException e) {
            log.warn("가게 수정 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 수정 처리 중 오류 발생", e);
            throw new StoreExceptions.StoreServiceException("가게 수정 처리 중 오류가 발생했습니다.");
        }
    }


    /**
     * 가게 정보 조회
     */
    @Override
    public StoreInfoResponse getStoreInfo(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(() -> new StoreExceptions.StoreNotFoundException());

            // 가게 이미지 조회
            List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
            List<String> ownerPickImages = imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);

            // 태그 조회
            List<StoreTagResponse> tags = storeTagRelationRepository.findTagsByStoreId(storeId)
                    .stream()
                    .map(StoreTagResponse::fromEntity)
                    .toList();

            // 가게 링크 및 대표 링크 조회
            Pair<List<String>, String> linkInfo = storeLinkService.getStoreLinksAndPrimary(storeId);

            // 운영 시간 조회
            List<OperatingHourResponse> operatingHourResponses = storeScheduleService.getOperatingHoursResponse(storeId);

            // 휴무일 조회
            List<HolidayResponse> holidayResponses = storeScheduleService.getHolidaysResponse(storeId);

            // 공지사항 조회
            StoreNoticeResponse noticeResponse = storeNoticeService.getLatestNotice(storeUuid);

            // 메뉴 리스트 조회
            List<MenuResponse> menus = menuService.getMenusByStore(storeUuid);

            return StoreInfoResponse.fromEntity(
                    store,
                    operatingHourResponses,
                    holidayResponses,
                    noticeResponse,
                    menus,
                    storeImages,
                    ownerPickImages,
                    tags,
                    linkInfo.getLeft(),
                    linkInfo.getRight()
            );
        } catch (StoreExceptions.StoreInfoReadFailedException e){
            log.warn("가게 정보 조회 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 정보 조회 처리 중 오류 발생", e);
            throw new StoreExceptions.StoreServiceException("가게 정보 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 삭제 */
    @Override
    public void deleteStore(UUID storeUuid) {
        try{
            UserEntity user = userService.getCurrentUser();
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                    .orElseThrow(StoreExceptions.StoreNotFoundException::new);

            if (!store.getOwnerUuid().equals(user.getUserUuid())) {
                throw new UserExceptions.UnauthorizedAccessException();
            }

            // 관련 엔티티 삭제
            deleteRelatedData(storeId);

            imageService.deleteImagesByRefId(ImageType.OWNERPICK, storeId);
            imageService.deleteImagesByRefId(ImageType.STORE, storeId);

            store.softDelete();
            storeRepository.save(store);
            storeSearchService.indexStore(storeId);
        } catch (StoreExceptions.StoreDeleteException e){
            log.warn("가게 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 삭제 처리 중 오류 발생", e);
            throw new StoreExceptions.StoreServiceException("가게 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    private void deleteRelatedData(Long storeId) {
        try{
            // 1. SavedStore 삭제
            savedStoreRepository.deleteByStoreId(storeId);

            // 2. StoreOperatingHour → StoreBreakTime → 삭제
            storeBreakTimeRepository.deleteAllByStoreId(storeId);
            storeOperatingHourRepository.deleteByStoreId(storeId);

            // 3. StoreHoliday 삭제
            storeHolidayRepository.deleteByStoreId(storeId);

            // 4. StoreLink 삭제
            storeLinkRepository.deleteByStoreId(storeId);

            // 4-2. StoreNotice soft delete
            List<StoreNotice> nos = storeNoticeRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);
            for (StoreNotice no : nos) {
                no.softDelete();
            }
            storeNoticeRepository.saveAll(nos);

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
        } catch (StoreExceptions.StoreDeleteException e){
            log.warn("가게 연관 데이터 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 연관 데이터 삭제 처리 중 오류 발생", e);
            throw new StoreExceptions.StoreServiceException("가게 삭제 처리 중 오류가 발생했습니다.");
        }
    }
}
