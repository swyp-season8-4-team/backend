package org.swyp.dessertbee.preference.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "선호도 응답 DTO")
public class PreferenceResponseDto {

    @Schema(description = "선호도 ID", example = "1")
    private Long id;

    @Schema(description = "선호도 이름", example = "비건")
    private String preferenceName;

    @Schema(description = "선호도 설명", example = "동물성 재료 없이!")
    private String preferenceDesc;
}