package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;
import org.swyp.dessertbee.statistics.store.entity.StoreViewLog;
import org.swyp.dessertbee.statistics.store.event.StoreViewEvent;
import org.swyp.dessertbee.statistics.store.repostiory.StoreViewLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreViewLogEventHandler {

    private final StoreViewLogRepository storeViewLogRepository;

    @Async
    @Transactional
    public void handleStoreViewAction(StoreViewEvent event) {
        try {
            storeViewLogRepository.save(
                    StoreViewLog.builder()
                            .storeId(event.getStoreId())
                            .userUuid(event.getUserUuid())
                            .viewedAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[가게 상세 조회 로그] 저장 실패: storeId={}, userUuid={}", event.getStoreId(), event.getUserUuid(), e);
            throw new StoreViewLogFailedException();
        }
    }
}