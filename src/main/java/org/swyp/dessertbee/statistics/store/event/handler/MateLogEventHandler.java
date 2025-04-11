package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.store.entity.DessertMateLog;
import org.swyp.dessertbee.statistics.store.event.MateActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.DessertMateLogRepository;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MateLogEventHandler {

    private final DessertMateLogRepository dessertMateLogRepository;

    @Async
    @Transactional
    public void handleMateAction(MateActionEvent event) {
        try {
            dessertMateLogRepository.save(
                    DessertMateLog.builder()
                            .storeId(event.getStoreId())
                            .mateId(event.getMateId())
                            .userUuid(event.getUserUuid())
                            .action(event.getAction()) // CREATE 또는 DELETE
                            .actionAt(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[메이트 로그] 저장 실패: storeId={}, mateId={}, userUuid={}, action={}", event.getStoreId(), event.getMateId(), event.getUserUuid(), event.getAction(), e);
            throw new MateLogCreateFailedException();
        }
    }
}