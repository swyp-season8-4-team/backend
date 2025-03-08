package org.swyp.dessertbee.community.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "community_review_statistics")
public class ReviewStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statisticsId;

    @Column(name = "review_id")
    private Long reviewId;

    private Integer views;
    private Integer saves;
    private Integer reviews;
    private LocalDate createDate;
    private LocalDateTime createdAt;

    public void count(int views){
        this.views = views;
    }
}
