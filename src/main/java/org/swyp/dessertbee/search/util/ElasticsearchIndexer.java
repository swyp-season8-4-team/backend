//package org.swyp.dessertbee.search.util;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.indices.ExistsRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.swyp.dessertbee.search.doc.StoreDocument;
//import org.swyp.dessertbee.search.exception.SearchExceptions.*;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class ElasticsearchIndexer {
//
//    private final ElasticsearchClient client;
//
//    public void createIndexIfNotExists(String indexName) {
//        try {
//            boolean exists = client.indices()
//                    .exists(ExistsRequest.of(e -> e.index(indexName)))
//                    .value();
//
//            if (!exists) {
//                client.indices().create(c -> c
//                        .index(indexName)
//                        .mappings(m -> m
//                                .properties("storeId", p -> p.long_(l -> l))
//                                .properties("storeUuid", p -> p.keyword(k -> k))
//                                .properties("storeName", p -> p.text(t -> t))
//                                .properties("address", p -> p.text(t -> t))
//                                .properties("tagNames", p -> p.keyword(k -> k))
//                                .properties("menuNames", p -> p.keyword(k -> k))
//                                .properties("deleted", p -> p.boolean_(b -> b))
//                        )
//                );
//            }
//
//        } catch (IOException e) {
//            throw new ElasticsearchCommunicationException("Elasticsearch 인덱스 생성 실패", e);
//        }
//    }
//
//    public void indexStoreDocument(String indexName, String id, StoreDocument doc) {
//        try {
//            client.index(i -> i.index(indexName).id(id).document(doc));
//        } catch (IOException e) {
//            throw new ElasticsearchCommunicationException("Elasticsearch 색인 실패 - id=" + id, e);
//        }
//    }
//}
