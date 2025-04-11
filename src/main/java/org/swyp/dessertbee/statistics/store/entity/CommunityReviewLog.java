package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_review_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private Long reviewId;

    private UUID userUuid;

    @Enumerated(EnumType.STRING)
    private ReviewAction action; // CREATE / DELETE

    private LocalDateTime actionAt;
}
