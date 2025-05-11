package org.swyp.dessertbee.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.store.preference.service.StoreTopTagBatchTriggerService;


@Component
@RequiredArgsConstructor
@Slf4j
public class StoreTopTagScheduler {

    private final StoreTopTagBatchTriggerService storeTopTagBatchTriggerService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul") // 매일 새벽 3시
    public void runTopTagBatch() {
        storeTopTagBatchTriggerService.executeBatch();
    }
}