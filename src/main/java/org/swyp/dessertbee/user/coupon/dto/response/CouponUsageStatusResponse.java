package org.swyp.dessertbee.user.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
@Schema(description = "쿠폰 사용 상태 통계 응답")
public class CouponUsageStatusResponse {
    @Schema(description = "사용된 쿠폰 수")
    private long usedCount;

    @Schema(description = "사용되지 않은 쿠폰 수")
    private long unusedCount;

    @Schema(description = "만료된 쿠폰 수")
    private long expiredCount;
}
