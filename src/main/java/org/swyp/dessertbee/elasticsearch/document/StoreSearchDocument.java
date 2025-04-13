package org.swyp.dessertbee.elasticsearch.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Document(indexName = "stores")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreSearchDocument {

    @Id
    private Long storeId;

    @Field(type = FieldType.Text, analyzer = "nori") // 한글 분석기
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> menuNames;

    @GeoPointField
    private GeoPoint location;
}
