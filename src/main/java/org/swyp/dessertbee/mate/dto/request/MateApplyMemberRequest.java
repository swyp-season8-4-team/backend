package org.swyp.dessertbee.mate.dto.request;


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
public class MateApplyMemberRequest {

    @NotNull
    private UUID creatorUserUuid;

    //디저트메이트 거절할 때
    private UUID rejectUserUuid;

    //디저트메이트 수락할 때
    private UUID acceptUserUuid;

    //디저트메이트 강퇴 시킬 떄
    private UUID banUserUuid;
}
