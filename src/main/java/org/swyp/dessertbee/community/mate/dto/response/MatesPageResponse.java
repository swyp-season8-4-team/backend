package org.swyp.dessertbee.community.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MatesPageResponse {

    private List<MateDetailResponse> mates;
    private boolean isLast;
}
