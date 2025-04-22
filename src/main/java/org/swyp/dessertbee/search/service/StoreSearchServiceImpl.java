package org.swyp.dessertbee.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.search.doc.StoreDocument;
import org.swyp.dessertbee.search.util.ElasticsearchIndexer;
import org.swyp.dessertbee.search.util.StoreDocumentFactory;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class StoreSearchServiceImpl implements StoreSearchService {

    private final StoreRepository storeRepository;
    private final StoreDocumentFactory storeDocumentFactory;
    private final ElasticsearchIndexer elasticsearchIndexer;

    private static final String INDEX_NAME = "stores";

    @Override
    public void migrateStoresToElasticsearch() {
        elasticsearchIndexer.createIndexIfNotExists(INDEX_NAME);

        List<Store> stores = storeRepository.findAll();
        for (Store store : stores) {
            StoreDocument doc = storeDocumentFactory.fromStore(store);
            elasticsearchIndexer.indexStoreDocument(INDEX_NAME, store.getStoreId().toString(), doc);
        }
    }

    @Override
    public void indexStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

        StoreDocument doc = storeDocumentFactory.fromStore(store);
        elasticsearchIndexer.indexStoreDocument(INDEX_NAME, storeId.toString(), doc);
    }
}