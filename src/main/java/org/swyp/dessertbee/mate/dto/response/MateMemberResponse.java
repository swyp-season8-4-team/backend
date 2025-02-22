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

    private Long mateId;
    private UUID mateUuid;
    private Long userId;
    private UUID userUUid;
    private String grade;
    private Boolean approvalYn;
    private Boolean removeYn;
    private List<String> profileImage;
    private String nickname;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UUID mateUuid,
                                                UserEntity user,
                                                List<String> profileImage) {
        return MateMemberResponse.builder()
                .mateId(member.getMateId())
                .mateUuid(mateUuid)
                .userId(user.getId())
                .userUUid(user.getUserUuid())
                .grade(member.getGrade().toString())
                .approvalYn(member.getApprovalYn())
                .removeYn(member.getRemoveYn())
                .profileImage(profileImage)
                .nickname(user.getNickname())
                .build();
    }
}
