package org.swyp.dessertbee.mate.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.entity.MateMember;

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
    private List<String> userImage;
    private String nickname;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UUID mateUuid,
                                                UUID userUUid,
                                                List<String> userImage,
                                                String nickname) {

        return MateMemberResponse.builder()
                .mateUuid(mateUuid)
                .userUUid(userUUid)
                .grade(member.getGrade().toString())
                .approvalYn(member.getApprovalYn())
                .userImage(userImage)
                .nickname(nickname)
                .build();
    }
}
