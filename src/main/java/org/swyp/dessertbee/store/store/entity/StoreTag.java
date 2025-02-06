package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 태그 고유번호

    @Column(nullable = false, unique = true, length = 100)
    private String name; // 태그명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TagCategory category; // 태그 카테고리
}
