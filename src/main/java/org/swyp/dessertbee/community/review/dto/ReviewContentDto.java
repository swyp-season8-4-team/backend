package org.swyp.dessertbee.community.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewContentDto {
    private Long id;
    private String type; // "text" 또는 "image"
    private String value; // 텍스트 내용 (type이 "text"인 경우)
    private Long imageId;
    private UUID imageUuid;// 이미지 순서 (type이 "image"인 경우)\
    private String imageUrl;
}
