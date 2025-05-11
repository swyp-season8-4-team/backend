package org.swyp.dessertbee.user.coupon.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
@Schema(description = "특정 가게의 쿠폰별 사용자 발급 여부 응답")
public class CouponIssuedStatusResponse {

    @Schema(description = "쿠폰 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long couponId;

    @Schema(description = "쿠폰 이름", example = "아메리카노 증정 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "사용자가 해당 쿠폰을 발급받았는지 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean issued;
}