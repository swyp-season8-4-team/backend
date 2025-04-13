package org.swyp.dessertbee.store.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.notice.dto.request.StoreNoticeRequest;
import org.swyp.dessertbee.store.notice.dto.response.StoreNoticeResponse;
import org.swyp.dessertbee.store.notice.entity.StoreNotice;
import org.swyp.dessertbee.store.notice.repository.StoreNoticeRepository;
import org.swyp.dessertbee.store.notice.exception.StoreNoticeExceptions.*;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.UUID;

/**
 * StoreNoticeService 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreNoticeServiceImpl implements StoreNoticeService{

    private final StoreNoticeRepository storeNoticeRepository;
    private final StoreRepository storeRepository;

    /** 공지 추가 */
    public void createNotice(UUID storeUuid, StoreNoticeRequest request){
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            StoreNotice notice = StoreNotice.builder()
                    .storeId(storeId)
                    .tag(request.tag())
                    .title(request.title())
                    .content(request.content())
                    .build();

            storeNoticeRepository.save(notice);
        } catch (StoreNoticeCreationFailedException e){
            log.warn("가게 공지 추가 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 공지 추가 처리 중 오류 발생", e);
            throw new StoreNoticeServiceException("가게 공지 추가 처리 중 오류가 발생했습니다.");
        }
    }

    /** 가게 공지 리스트 조회 */
    public List<StoreNoticeResponse> getNoticesByStoreUuid(UUID storeUuid){
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }
            List<StoreNotice> notices = storeNoticeRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);

            return notices.stream()
                    .map(n -> new StoreNoticeResponse(
                            n.getNoticeId(),
                            n.getTag(),
                            n.getTitle(),
                            n.getContent(),
                            n.getCreatedAt(),
                            n.getUpdatedAt()
                    ))
                    .toList();
        } catch (StoreNoticeNotFoundException e) {
            log.warn("가게 공지 리스트 조회 실패 - storeUuid: {}, 사유: {}", storeUuid, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 공지 리스트 조회 처리 중 오류 발생", e);
            throw new StoreNoticeServiceException("가게 공지 리스트 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 특정 공지 조회 */
    public StoreNoticeResponse getNotice(Long noticeId){
        try {
            StoreNotice notice = storeNoticeRepository.findByNoticeIdAndDeletedAtIsNull(noticeId);
            if (notice == null) {
                throw new StoreNoticeNotFoundException();
            }

            return new StoreNoticeResponse(
                    notice.getNoticeId(),
                    notice.getTag(),
                    notice.getTitle(),
                    notice.getContent(),
                    notice.getCreatedAt(),
                    notice.getUpdatedAt()
            );

        } catch (StoreNoticeNotFoundException e) {
            log.warn("공지 조회 실패 - noticeId: {}, 사유: {}", noticeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 공지 조회 처리 중 오류 발생", e);
            throw new StoreNoticeServiceException("가게 공지 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 특정 공지 수정 */
    public StoreNoticeResponse updateNotice(UUID storeUuid, Long noticeId, StoreNoticeRequest request){
        try {
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            StoreNotice notice = storeNoticeRepository.findByNoticeIdAndDeletedAtIsNull(noticeId);
            if (notice == null || !notice.getStoreId().equals(storeId)) {
                throw new StoreNoticeNotFoundException();
            }

            notice.update(request.title(), request.content(), request.tag());

            return new StoreNoticeResponse(
                    notice.getNoticeId(),
                    notice.getTag(),
                    notice.getTitle(),
                    notice.getContent(),
                    notice.getCreatedAt(),
                    notice.getUpdatedAt()
            );

        } catch (StoreNoticeNotFoundException | InvalidStoreUuidException | StoreNoticeUpdateFailedException e) {
            log.warn("공지 수정 실패 - storeUuid: {}, noticeId: {}, 사유: {}", storeUuid, noticeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("공지 수정 처리 중 오류 발생", e);
            throw new StoreNoticeServiceException("공지 수정 중 오류가 발생했습니다.");
        }
    }

    /** 특정 공지 삭제 */
    public void deleteNotice(UUID storeUuid, Long noticeId){
        try {
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            StoreNotice notice = storeNoticeRepository.findByNoticeIdAndDeletedAtIsNull(noticeId);
            if (notice == null || !notice.getStoreId().equals(storeId)) {
                throw new StoreNoticeNotFoundException();
            }

            notice.softDelete();

        } catch (StoreNoticeNotFoundException | InvalidStoreUuidException | StoreNoticeDeleteFailedException e) {
            log.warn("공지 삭제 실패 - storeUuid: {}, noticeId: {}, 사유: {}", storeUuid, noticeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("공지 삭제 처리 중 오류 발생", e);
            throw new StoreNoticeServiceException("공지 삭제 중 오류가 발생했습니다.");
        }
    }
}
