package org.swyp.dessertbee.store.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store_review")
public class StoreReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "review_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID reviewUuid;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "user_uuid")
    private UUID userUuid;

    private String content;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public void updateContentAndRating(String content, BigDecimal rating) {
        this.content = content;
        this.rating = rating;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
