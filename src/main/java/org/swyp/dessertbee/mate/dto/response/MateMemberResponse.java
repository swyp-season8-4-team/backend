package org.swyp.dessertbee.mate.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateMemberResponse {

    private UUID mateUuid;
    private UUID userUUid;
    private String grade;
    private Boolean approvalYn;
    private Boolean removeYn;
    private List<String> userImage;
    private String nickname;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UUID mateUuid,
                                                UserEntity user,
                                                List<String> userImage) {
        return MateMemberResponse.builder()
                .mateUuid(mateUuid)
                .userUUid(user.getUserUuid())
                .grade(member.getGrade().toString())
                .approvalYn(member.getApprovalYn())
                .removeYn(member.getRemoveYn())
                .userImage(userImage)
                .nickname(user.getNickname())
                .build();
    }
}
