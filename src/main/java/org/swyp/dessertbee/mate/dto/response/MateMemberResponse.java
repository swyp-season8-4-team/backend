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
    private Long userId;
    private UUID userUUid;
    private String grade;
    private Boolean approvalYn;
    private Boolean bannedYn;
    private List<String> profileImage;
    private String nickname;
    private UserEntity.Gender gender;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UUID mateUuid,
                                                UserEntity user,
                                                List<String> profileImage) {
        return MateMemberResponse.builder()
                .mateUuid(mateUuid)
                .userId(user.getId())
                .userUUid(user.getUserUuid())
                .gender(user.getGender())
                .grade(member.getGrade().toString())
                .approvalYn(member.getApprovalYn())
                .bannedYn(member.getBannedYn())
                .profileImage(profileImage)
                .nickname(user.getNickname())
                .build();
    }
}
