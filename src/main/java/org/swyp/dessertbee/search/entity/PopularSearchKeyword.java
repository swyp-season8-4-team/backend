package org.swyp.dessertbee.search.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "popular_search_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PopularSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyword;

    @Column(nullable = false)
    private int searchCount;

    public void incrementCount(int count) {
        this.searchCount += count;
    }

    public static PopularSearchKeyword create(String keyword) {
        return PopularSearchKeyword.builder()
                .keyword(keyword)
                .searchCount(0)
                .build();
    }
}