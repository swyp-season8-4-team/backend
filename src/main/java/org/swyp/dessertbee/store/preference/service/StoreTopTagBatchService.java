package org.swyp.dessertbee.store.preference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.preference.repository.StoreTopTagRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreTopTagBatchService {

    private final StoreTopTagRepository storeTopTagRepository;

    @Transactional
    public void refreshStoreTopTags() {
        log.info("[배치] store_top_tag 초기화 시작");
        storeTopTagRepository.truncateStoreTopTag();

        log.info("[스케줄러 시작] 통계 집계 at {}", LocalDateTime.now());
        storeTopTagRepository.populateStoreTopTag();

        log.info("[스케줄러 종료] 통계 집계 완료 at {}", LocalDateTime.now());
    }
}