package org.swyp.dessertbee.community.review.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponse {
    private UUID reviewUuid;
    private UUID userUuid;
    private String nickname;
    private String profileImage;
    private String thumbnail; // 리뷰 첫 번째 이미지 (썸네일)
    private String title;
    private String content; // 리뷰 글 내용
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}