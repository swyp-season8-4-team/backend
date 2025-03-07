package org.swyp.dessertbee.community.review.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.swyp.dessertbee.community.mate.entity.Mate;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_community_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_review_id")
    private Long savedReviewId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", referencedColumnName = "review_id", nullable = false)
    private Review review;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
