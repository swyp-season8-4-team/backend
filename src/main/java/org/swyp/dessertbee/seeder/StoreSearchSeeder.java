package org.swyp.dessertbee.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.elasticsearch.document.StoreSearchDocument;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreSearchSeeder implements CommandLineRunner {

    private final ElasticsearchOperations operations;

    @Override
    public void run(String... args) {
        StoreSearchDocument doc = StoreSearchDocument.builder()
                .storeId(1L)
                .name("디저트비 합정점")
                .address("서울 마포구 양화로 23길")
                .tags(List.of("맛집", "케이크"))
                .menuNames(List.of("수건 케이크", "티라미수"))
                .location(new GeoPoint(37.55491076, 126.92371503))
                .build();

        operations.save(doc);
    }
}