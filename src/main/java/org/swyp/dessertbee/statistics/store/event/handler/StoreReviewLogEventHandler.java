package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.store.entity.StoreReviewLog;
import org.swyp.dessertbee.statistics.store.event.StoreReviewActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.StoreReviewLogRepository;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreReviewLogEventHandler {

    private final StoreReviewLogRepository storeReviewLogRepository;

    @Async
    @Transactional
    public void handleStoreReviewLogAction(StoreReviewActionEvent event) {
        try {
            storeReviewLogRepository.save(
                    StoreReviewLog.builder()
                            .storeId(event.getStoreId())
                            .reviewId(event.getReviewId())
                            .userUuid(event.getUserUuid())
                            .action(event.getAction()) // CREATE 또는 DELETE
                            .actionAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[한줄리뷰 로그] 저장 실패: storeId={}, reviewId={}, userUuid={}, action={}", event.getStoreId(), event.getReviewId(), event.getUserUuid(), event.getAction(), e);
            throw new StoreReviewLogCreateFailedException();
        }
    }
}