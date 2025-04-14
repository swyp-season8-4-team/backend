package org.swyp.dessertbee.community.mate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateReplyCreateRequest {


    @NotBlank(message = "댓글 작성하는 사람의 uuid를 넘겨주세요.")
    @Schema(description = "댓글 작성하는 사람 uuid", example = "19a40ec1-ac92-419e-aa2b-0fcfcbd42447")
    private UUID userUuid;

    @Schema(description = "디저트메이트 상위 댓글 id(앱전용)", example = "3")
    private Long parentMateReplyId;

    @NotBlank(message = "디저트메이트 댓글 내용을 작성해주세요.")
    @Schema(description = "디저트메이트 댓글 내용 작성", example = "홍대 빵지순례 리스트 200개 있습니다. 같이 맛있는거 먹으러 가요.")
    private String content;



}
