package org.swyp.dessertbee.elasticsearch.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.elasticsearch.document.StoreSearchDocument;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.service.StoreTagService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreSearchInitializer {

    private final StoreRepository storeRepository;
    private final ElasticsearchOperations operations;
    private final StoreTagService storeTagService;
    private final MenuService menuService;

    @PostConstruct
    public void initialize() {
        List<Store> stores = storeRepository.findAll();

        List<StoreSearchDocument> docs = stores.stream().map(store ->
                StoreSearchDocument.builder()
                        .storeId(store.getStoreId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .tags(storeTagService.getTags(store.getStoreId()))
                        .menuNames(menuService.getMenuNames(store.getStoreId()))
                        .build()
        ).toList();

        operations.save(docs);
    }
}
