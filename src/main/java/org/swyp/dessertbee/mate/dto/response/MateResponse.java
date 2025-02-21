package org.swyp.dessertbee.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
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
    private int currentMembers;
    private String nickname;
    private Boolean recruitYn;
}
