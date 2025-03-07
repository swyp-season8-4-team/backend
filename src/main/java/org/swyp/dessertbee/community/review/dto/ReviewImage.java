package org.swyp.dessertbee.community.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {

    private String reviewImages;

    private Long reviewImageId;
}
