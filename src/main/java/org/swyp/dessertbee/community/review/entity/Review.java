package org.swyp.dessertbee.community.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.swyp.dessertbee.community.review.dto.request.ReviewUpdateRequest;
import org.swyp.dessertbee.store.store.entity.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="community_review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "review_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID reviewUuid;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "review_category_id")
    private Long reviewCategoryId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "place_name")
    private String placeName;

    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Review와 ReviewContent 간 1:N 관계 매핑 (unidirectional)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "review_id", referencedColumnName = "review_id")
    private List<ReviewContent> reviewContents;

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }

    public void update(ReviewUpdateRequest request, Store store) {
        this.title = request.getTitle();
        this.reviewCategoryId = request.getReviewCategoryId();
        if (store != null) {
            this.storeId = store.getStoreId();
            this.placeName = store.getName();
            this.latitude = store.getLatitude();
            this.longitude = store.getLongitude();
            this.address = store.getAddress();
        }
    }
}
