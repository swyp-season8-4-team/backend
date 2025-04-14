package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MateAppReplyResponse {


    @NotBlank
    @Schema(description = "디저트메이트 댓글 id", example = "2")
    private Long mateReplyId;

    @NotBlank
    @Schema(description = "디저트메이트 uuid", example = "3037ab04-195e-48d1-83e2-e005899fc74d")
    private UUID mateUuid;

    @Schema(description = "디저트메이트 상위 댓글 id(앱전용)", example = "3")
    private Long parentMateReplyId;


    @NotBlank
    @Schema(description = "댓글 작성하는 사람 uuid",  example = "19a40ec1-ac92-419e-aa2b-0fcfcbd42447")
    private UUID userUuid;

    @NotBlank
    @Schema(description = "댓글 작성하는 사람 닉네임",  example = "디저비1")
    private String nickname;

    @NotBlank
    @Schema(description = "댓글 작성 내용 작성",  example = "홍대 빵지순례 리스트 200개 있습니다. 같이 맛있는거 먹으러 가요.")
    private String content;

    @NotBlank
    @Schema(description = "댓글 작성자 프로필 이미지", example = " mateImage=: https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/profile/75/7edd7706-0bfa-46cf-a6c2-ad67f8a9a440-IMG_8828.jpeg")
    private String profileImage;

    @NotBlank
    @Schema(description = "댓글 작성자 성별", example = "FEMALE")
    private UserEntity.Gender gender;

    @NotNull
    @Schema(description = "댓글 생성 날짜", example = "2025-03-10 02:15")
    private LocalDateTime createdAt;

    @Schema(description = "댓글 수정 날짜", example = "2025-03-10 02:44")
    private LocalDateTime updatedAt;


    @Schema(description = "대댓글 리스트 (자식 댓글)", implementation = MateAppReplyResponse.class)
    private List<MateAppReplyResponse> children;


    public static MateAppReplyResponse fromEntity(MateReply reply,
                                               UUID mateUuid,
                                               UserEntity user,
                                               String profileImage,
                                               List<MateAppReplyResponse> children
    ) {

        return MateAppReplyResponse.builder()
                .mateReplyId(reply.getMateReplyId())
                .mateUuid(mateUuid)
                .nickname(user.getNickname())
                .parentMateReplyId(reply.getParentMateReplyId())
                .gender(user.getGender())
                .userUuid(user.getUserUuid())
                .content(reply.getContent())
                .profileImage(profileImage)
                .children(children)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();

    }
}
