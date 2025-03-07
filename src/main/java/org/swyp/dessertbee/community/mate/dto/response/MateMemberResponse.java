package org.swyp.dessertbee.community.mate.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.community.mate.entity.MateMember;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateMemberResponse {

    private Long userId;
    private UUID userUuid;
    private String grade;
    private List<String> profileImage;
    private String nickname;
    private UserEntity.Gender gender;
    private MateApplyStatus applyStatus;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UserEntity user,
                                                List<String> profileImage) {
        return MateMemberResponse.builder()
                .userId(user.getId())
                .userUuid(user.getUserUuid())
                .gender(user.getGender())
                .grade(member.getGrade().toString())
                .applyStatus(member.getApplyStatus())
                .profileImage(profileImage)
                .nickname(user.getNickname())
                .build();
    }
}
