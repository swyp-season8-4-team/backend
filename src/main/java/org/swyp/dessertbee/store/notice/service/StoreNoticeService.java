package org.swyp.dessertbee.store.notice.service;

import org.swyp.dessertbee.store.notice.dto.request.StoreNoticeRequest;
import org.swyp.dessertbee.store.notice.dto.response.StoreNoticeResponse;

import java.util.List;
import java.util.UUID;

public interface StoreNoticeService {
    /** 공지 추가 */
    void createNotice(UUID storeUuid, StoreNoticeRequest request);

    /** 가게 공지 리스트 조회 */
    List<StoreNoticeResponse> getNoticesByStoreUuid(UUID storeUuid);

    /** 특정 공지 조회 */
    StoreNoticeResponse getNotice(Long noticeId);

    /** 특정 공지 수정 */
    StoreNoticeResponse updateNotice(UUID storeUuid, Long noticeId, StoreNoticeRequest request);

    /** 특정 공지 삭제 */
    void deleteNotice(UUID storeUuid, Long noticeId);
}
