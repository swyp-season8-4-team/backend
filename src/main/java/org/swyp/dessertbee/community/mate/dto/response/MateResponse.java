package org.swyp.dessertbee.community.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateResponse {
    private UUID mateUuid;
    private String mateCategory;
    private String thumbnail;
    private String title;
    private String content;
    private String nickname;
    private Boolean recruitYn;
    private boolean saved;
}
