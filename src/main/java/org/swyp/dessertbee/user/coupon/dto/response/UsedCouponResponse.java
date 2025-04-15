package org.swyp.dessertbee.user.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
@Getter
@Schema(description = "사용된 쿠폰 응답")
public class UsedCouponResponse {
    @Schema(description = "사용자 쿠폰 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userCouponId;

    @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "쿠폰 코드", example = "ZH1i6D", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userCouponCode;

    @Schema(description = "사용 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean used;
}
