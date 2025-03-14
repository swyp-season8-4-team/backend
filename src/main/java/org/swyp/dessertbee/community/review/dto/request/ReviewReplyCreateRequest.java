package org.swyp.dessertbee.community.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyCreateRequest {

    @NotNull
    private UUID userUuid;

    @NotNull
    private String content;

    private String report;
}
