package org.swyp.dessertbee.community.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review_content")
public class ReviewContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId; // Review 엔티티와 연관

    @Column(nullable = false)
    private String type; // "text" 또는 "image"

    @Column(length = 1000)
    private String value; // 텍스트 내용 또는 이미지 URL 등

    @Column(name = "image_index")
    private Integer imageIndex;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
