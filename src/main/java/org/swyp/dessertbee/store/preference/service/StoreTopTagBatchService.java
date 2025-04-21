package org.swyp.dessertbee.store.preference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.preference.repository.StoreTopTagRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreTopTagBatchService {

    private final StoreTopTagRepository storeTopTagRepository;

    @Transactional
    public void refreshStoreTopTags() {
        log.info("[배치] store_top_tag 초기화 시작");
        storeTopTagRepository.truncateStoreTopTag();

        log.info("[배치] store_top_tag 집계 및 저장 시작");
        storeTopTagRepository.populateStoreTopTag();

        log.info("[배치] store_top_tag 배치 완료");
    }
}