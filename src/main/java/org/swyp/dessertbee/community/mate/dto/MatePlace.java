package org.swyp.dessertbee.community.mate.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatePlace {

    @Schema(description = "디저트메이트 지정 장소 이름", example = "키토빵앗간")
    private String placeName;

    @Schema(description = "디저트메이트 지정 장소 주소", example = "서울 마포구 와우산로29길 47 1층")
    private String address;

    @Schema(description = "디저트메이트 지정 장소 위도", example = "37.55564710")
    private BigDecimal  latitude;

    @Schema(description = "디저트메이트 지정 장소 경도", example = "126.92734908")
    private BigDecimal  longitude;

}
