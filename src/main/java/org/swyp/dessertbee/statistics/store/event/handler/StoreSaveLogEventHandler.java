package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;
import org.swyp.dessertbee.statistics.store.entity.StoreSaveLog;
import org.swyp.dessertbee.statistics.store.event.StoreSaveActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.StoreSaveLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreSaveLogEventHandler {

    private final StoreSaveLogRepository storeSaveLogRepository;

    @Async
    @Transactional
    public void handleStoreSaveAction(StoreSaveActionEvent event) {
        try {
            storeSaveLogRepository.save(
                    StoreSaveLog.builder()
                            .storeId(event.getStoreId())
                            .userUuid(event.getUserUuid())
                            .action(event.getAction()) // SAVE 또는 UNSAVE
                            .actionAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[가게 저장 관련 로그] 저장 실패: storeId={}, userUuid={}, action={}", event.getStoreId(), event.getUserUuid(), event.getAction(), e);
            throw new StoreSaveLogCreateFailedException();
        }
    }
}