package org.swyp.dessertbee.community.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ReviewPageResponse {

    private List<ReviewResponse> reviews;
    private boolean isLast;
}
