package org.swyp.dessertbee.common.dto;

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
public class ReportRequest {

    @NotNull(message = "작성하는 사람의 uuid를 넘겨주세요.")
    @Schema(description = "작성하는 사람 uuid", example = "19a40ec1-ac92-419e-aa2b-0fcfcbd42447")
    private UUID userUuid;

    @NotNull(message = "신고 카테고리 선택해주세요.")
    @Schema(description = "신고 카테고리", example = "1")
    private Long reportCategoryId;

    @Schema(description = "신고 기타 내용", example = "저 사용자 자체에 문제가 있습니다.")
    private String reportComment;
}
