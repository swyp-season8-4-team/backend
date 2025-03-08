package org.swyp.dessertbee.community.mate.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.mate.entity.MateReply;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateReplyResponse {


    private Long mateReplyId;
    private UUID mateUuid;
    private String nickname;
    private UUID userUuid;
    private String content;
    private String profileImage;
    private UserEntity.Gender gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static MateReplyResponse fromEntity(MateReply reply,
                                               UUID mateUuid,
                                               UserEntity user,
                                               String profileImage
                                               ) {

        return MateReplyResponse.builder()
                .mateReplyId(reply.getMateReplyId())
                .mateUuid(mateUuid)
                .nickname(user.getNickname())
                .gender(user.getGender())
                .userUuid(user.getUserUuid())
                .content(reply.getContent())
                .profileImage(profileImage)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();

    }
}
