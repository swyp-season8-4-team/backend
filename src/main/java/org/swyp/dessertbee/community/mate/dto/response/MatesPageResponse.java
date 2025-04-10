package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MatesPageResponse {


    @Schema(description = "디저트메이트 정보", example = "MateDetailResponse 전체")
    private List<MateDetailResponse> mates;

    @NotNull
    @Schema(description = "무한 스크롤 마지막 페이지 확인", example = "false")
    private boolean isLast;
}
