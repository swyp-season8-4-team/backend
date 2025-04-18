package org.swyp.dessertbee.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.search.doc.StoreDocument;
import org.swyp.dessertbee.store.menu.repository.MenuRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.search.exception.SearchExceptions.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreSearchServiceImpl implements StoreSearchService {

    private final StoreRepository storeRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;
    private final MenuRepository menuRepository;
    private final ElasticsearchClient client;

    /**
     * [1] 인덱스가 없으면 생성
     */
    public void createStoreIndexIfNotExists() {
        try {
            boolean exists = client.indices()
                    .exists(ExistsRequest.of(e -> e.index("stores")))
                    .value();

            if (!exists) {
                client.indices().create(c -> c
                        .index("stores")
                        .mappings(m -> m
                                .properties("storeId", p -> p.long_(l -> l))
                                .properties("storeUuid", p -> p.keyword(k -> k))
                                .properties("storeName", p -> p.text(t -> t))
                                .properties("address", p -> p.text(t -> t))
                                .properties("tagNames", p -> p.keyword(k -> k))
                                .properties("menuNames", p -> p.keyword(k -> k))
                                .properties("deleted", p -> p.boolean_(b -> b))
                        )
                );
                log.info("[Elasticsearch] 'stores' 인덱스 생성 완료");
            } else {
                log.info("[Elasticsearch] 'stores' 인덱스는 이미 존재합니다");
            }

        } catch (IOException e) {
            log.error("[Elasticsearch] 인덱스 존재 여부 확인/생성 실패", e);
            throw new ElasticsearchCommunicationException(
                    "Elasticsearch 인덱스 생성 중 IOException 발생", e
            );
        }
    }

    /**
     * [2] 초기 마이그레이션 (MySQL → Elasticsearch)
     */
    @Override
    @Transactional(readOnly = true)
    public void migrateStoresToElasticsearch() {
        createStoreIndexIfNotExists(); // 인덱스가 없으면 생성

        List<Store> stores = storeRepository.findAll();

        for (Store store : stores) {
            List<String> tagNames = storeTagRelationRepository.findTagNamesByStoreId(store.getStoreId());
            List<String> menuNames = menuRepository.findMenuNamesByStoreId(store.getStoreId());

            StoreDocument doc = StoreDocument.builder()
                    .storeId(store.getStoreId())
                    .storeUuid(store.getStoreUuid())
                    .storeName(store.getName())
                    .address(store.getAddress())
                    .tagNames(tagNames)
                    .menuNames(menuNames)
                    .deleted(store.getDeletedAt() != null)
                    .build();

            try {
                client.index(i -> i
                        .index("stores")
                        .id(store.getStoreId().toString())
                        .document(doc)
                );
            } catch (IOException e) {
                log.error("[Elasticsearch] 색인 실패 - storeId: {}", store.getStoreId(), e);
                throw new ElasticsearchCommunicationException(
                        "[Elasticsearch] 색인 실패 - storeId: " + store.getStoreId(), e
                );
            }
        }

        log.info("[Elasticsearch] 모든 가게 색인 완료");
    }

    /**
     * 가게 저장/수정 후 Elasticsearch 반영
     */
    @Override
    @Transactional(readOnly = true)
    public void indexStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException());

        List<String> tagNames = storeTagRelationRepository.findTagNamesByStoreId(storeId);
        List<String> menuNames = menuRepository.findMenuNamesByStoreId(storeId);

        StoreDocument doc = StoreDocument.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .storeName(store.getName())
                .address(store.getAddress())
                .tagNames(tagNames)
                .menuNames(menuNames)
                .deleted(store.getDeletedAt() != null)
                .build();

        try {
            client.index(i -> i
                    .index("stores")
                    .id(store.getStoreId().toString())
                    .document(doc)
            );
            log.info("[Elasticsearch] 가게 색인 완료 - storeId={}", storeId);
        } catch (IOException e) {
            log.error("[Elasticsearch] 가게 색인 실패 - storeId={}", storeId, e);
            throw new ElasticsearchCommunicationException(
                    "[Elasticsearch] 가게 색인 실패 - storeId: " + storeId, e
            );
        }
    }
}
