package org.swyp.dessertbee.store.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="store_review_report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_reivew_report_id")
    private long storeReviewReportId;

    @Column(name = "report_category_id")
    private Long reportCategoryId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "review_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID reviewUuid;

    @Column(name = "user_id")
    private Long userId;


    @Column(name = "comment")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
