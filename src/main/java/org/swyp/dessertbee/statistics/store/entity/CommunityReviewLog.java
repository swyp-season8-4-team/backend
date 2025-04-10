package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;

import java.time.LocalDateTime;

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

    private Long userId;
    // todo: 다른 가게 관련 로그와의 일관성을 위해 여기서도 userUuid를 저장하는게 좋을 것 같음 -> 커뮤니티 엔티티 수정 필요

    @Enumerated(EnumType.STRING)
    private ReviewAction action; // CREATE / DELETE

    private LocalDateTime actionAt;
}
