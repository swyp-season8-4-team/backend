package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.store.entity.CommunityReviewLog;
import org.swyp.dessertbee.statistics.store.event.CommunityReviewActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.CommunityReviewLogRepository;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityReviewLogEventHandler {

    private final CommunityReviewLogRepository communityReviewLogRepository;

    @Async
    @Transactional
    public void handleCommunityReviewLogAction(CommunityReviewActionEvent event) {
        try {
            communityReviewLogRepository.save(
                    CommunityReviewLog.builder()
                            .storeId(event.getStoreId())
                            .reviewId(event.getReviewId())
                            .userUuid(event.getUserUuid())
                            .action(event.getAction()) // CREATE 또는 DELETE
                            .actionAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[커뮤니티 리뷰 로그] 저장 실패: storeId={}, reviewId={}, userUuid={}, action={}", event.getStoreId(), event.getReviewId(), event.getUserUuid(), event.getAction(), e);
            throw new CommunityReviewLogCreateFailedException();
        }
    }
}