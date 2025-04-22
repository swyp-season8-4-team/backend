package org.swyp.dessertbee.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.store.preference.service.StoreTopTagBatchService;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreTopTagScheduler {

    private final StoreTopTagBatchService storeTopTagBatchService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void runTopTagBatch() {
        try {
            log.info("[스케줄러] store_top_tag 배치 시작");
            storeTopTagBatchService.refreshStoreTopTags();
            log.info("[스케줄러] store_top_tag 배치 종료");
        } catch (Exception e) {
            log.error("[스케줄러] store_top_tag 배치 실패", e);
        }
    }
}
