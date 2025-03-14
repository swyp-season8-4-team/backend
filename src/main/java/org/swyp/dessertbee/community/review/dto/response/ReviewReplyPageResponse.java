package org.swyp.dessertbee.community.review.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ReviewReplyPageResponse {

    private List<ReviewReplyResponse> reviews;
    private boolean isLast;
    private Long count;
}
