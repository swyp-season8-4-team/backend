package org.swyp.dessertbee.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MateReplyPageResponse {

    private List<MateReplyResponse> mates;
    private boolean isLast;
    private Long count;
}
