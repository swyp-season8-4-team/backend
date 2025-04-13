package org.swyp.dessertbee.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.elasticsearch.document.StoreSearchDocument;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StoreSearchService {

    private final ElasticsearchClient esClient;

    public List<StoreSearchDocument> search(Double latitude, Double longitude, Double radius, String keyword) {
        try {
            // 1. 검색어에 대한 bool query 생성
            Query keywordQuery = Query.of(q -> q.bool(b -> b
                    .should(s -> s.match(m -> m.field("name").query(keyword)))
                    .should(s -> s.match(m -> m.field("address").query(keyword)))
                    .should(s -> s.match(m -> m.field("tags").query(keyword)))
                    .should(s -> s.match(m -> m.field("menuNames").query(keyword)))
            ));

            // 2. Geo 필터 (위도/경도 + 반경)
            Query geoFilter = Query.of(q -> q.geoDistance(g -> g
                    .field("location") // 이건 mapping 시 lat/lon을 묶은 geo_point 필드여야 함
                    .distance(radius + "m")
                    .location(l -> l.latlon(latlon -> latlon
                            .lat(latitude)
                            .lon(longitude)))
                    .distanceType(GeoDistanceType.Arc)
            ));

            // 3. 최종 bool query (must + filter)
            Query finalQuery = Query.of(q -> q.bool(b -> b
                    .must(keywordQuery)
                    .filter(geoFilter)
            ));

            // 4. 검색 요청
            SearchResponse<StoreSearchDocument> response = esClient.search(SearchRequest.of(s -> s
                            .index("stores")
                            .query(finalQuery)
                    ),
                    StoreSearchDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 검색 중 오류 발생", e);
        }
    }
}