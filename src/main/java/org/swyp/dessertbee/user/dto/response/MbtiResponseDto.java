package org.swyp.dessertbee.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "MBTI 응답 DTO")
public class MbtiResponseDto {

    @Schema(description = "MBTI ID", example = "1")
    private Long id;

    @Schema(description = "MBTI 유형", example = "ENFP")
    private String mbtiType;

    @Schema(description = "MBTI 이름", example = "에너지벌")
    private String mbtiName;

    @Schema(description = "MBTI 설명", example = "신상과 트렌드를 따라가는 도전적인 디저트러버!")
    private String mbtiDesc;
}