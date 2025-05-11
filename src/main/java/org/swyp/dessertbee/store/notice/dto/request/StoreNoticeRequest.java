package org.swyp.dessertbee.store.notice.dto.request;

import org.swyp.dessertbee.store.notice.entity.NoticeTag;

public record StoreNoticeRequest(
        NoticeTag tag,
        String title,
        String content
) {}