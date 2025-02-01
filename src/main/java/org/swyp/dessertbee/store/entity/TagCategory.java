package org.swyp.dessertbee.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TagCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 카테고리 고유번호

    @Column(nullable = false, unique = true, length = 100)
    private String name; // 카테고리명
}
