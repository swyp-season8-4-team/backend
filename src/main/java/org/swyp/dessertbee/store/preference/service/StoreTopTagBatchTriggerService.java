package org.swyp.dessertbee.store.preference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.preference.service.StoreTopTagBatchService;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreTopTagBatchTriggerService {

    private final StoreTopTagBatchService storeTopTagBatchService;

    public void executeBatch() {
        try {
            log.info("[수동/자동 트리거] store_top_tag 배치 시작");
            storeTopTagBatchService.refreshStoreTopTags();
            log.info("[수동/자동 트리거] store_top_tag 배치 종료");
        } catch (Exception e) {
            log.error("[수동/자동 트리거] store_top_tag 배치 실패", e);
            throw e;
        }
    }
}