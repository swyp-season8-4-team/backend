package org.swyp.dessertbee.community.mate.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Schema(description = "디저트메이트 참여하는 멤버 id", example = "20")
    private Long userId;

    @NotNull
    @Schema(description = "디저트메이트 참여하는 사람 uuid",  example = "19a40ec1-ac92-419e-aa2b-0fcfcbd42447")
    private UUID userUuid;

    @NotBlank
    @Schema(description = "디저트메이트 참여하는 사람 등급(생성자 또는 일반 사용자)" , example = "NORMAL")
    private String grade;

    @NotBlank
    @Schema(description = "디저트메이트 생성자 프로필 이미지", example = " mateImage=: https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/profile/75/7edd7706-0bfa-46cf-a6c2-ad67f8a9a440-IMG_8828.jpeg")
    private String profileImage;

    @NotBlank
    @Schema(description = "작성하는 사람 닉네임",  example = "디저비1")
    private String nickname;

    @NotNull
    @Schema(description = "현재 로그인한 사용자의 디저트메이트 신청 상태값", example = "APPROVED")
    private MateApplyStatus applyStatus;

    @NotBlank
    @Schema(description = "디저트메이트 작성자 성별", example = "FEMALE")
    private UserEntity.Gender gender;

    public static MateMemberResponse fromEntity(MateMember member,
                                                UserEntity user,
                                                String profileImage) {
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
