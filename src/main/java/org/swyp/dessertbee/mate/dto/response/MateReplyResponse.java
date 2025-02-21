package org.swyp.dessertbee.mate.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateReply;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateReplyResponse {


    private Long mateReplyId;
    private Long mateId;
    private UUID mateUuid;
    private Long userId;
    private String nickname;
    private UUID userUuid;
    private String content;


    public static MateReplyResponse fromEntity(MateReply reply,
                                               UUID mateUuid,
                                               UserEntity user
                                               ) {

        return MateReplyResponse.builder()
                .mateReplyId(reply.getMateReplyId())
                .mateId(reply.getMateId())
                .mateUuid(mateUuid)
                .userId(reply.getUserId())
                .nickname(user.getNickname())
                .userUuid(user.getUserUuid())
                .content(reply.getContent())
                .build();

    }
}
