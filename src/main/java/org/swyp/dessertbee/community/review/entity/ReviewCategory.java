package org.swyp.dessertbee.community.review.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_review_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_category_id")
    private Long reviewCategoryId; // 카테고리 고유번호


    @Column(nullable = false, length = 100)
    private String name; //카테고리 이름
}
