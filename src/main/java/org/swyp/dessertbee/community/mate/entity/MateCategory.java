package org.swyp.dessertbee.community.mate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mate_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MateCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mate_category_id")
    private Long mateCategoryId; // 카테고리 고유번호


    @Column(nullable = false, length = 100)
    private String name; //카테고리 이름
}
