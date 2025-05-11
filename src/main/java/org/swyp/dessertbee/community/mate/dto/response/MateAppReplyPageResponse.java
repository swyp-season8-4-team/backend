package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
public class MateAppReplyPageResponse {


    @NotBlank
    @Schema(description = "디저트메이트 uuid", example = "3037ab04-195e-48d1-83e2-e005899fc74d")
    private UUID mateUuid;


    @Schema(description = "디저트메이트 댓글 정보", example = "MateReplyResponse에서 주는 값")
    private List<MateAppReplyResponse> mateReplies;

    @NotNull
    @Schema(description = "디저트메이트 내 댓글 무한 스크롤링 마지막 페이지 확인")
    private boolean isLast;

    @NotBlank
    @Schema(description = "댓글 갯수", defaultValue = "0")
    private Long count;


}
