package org.swyp.dessertbee.user.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "쿠폰 사용 요청")
public class UseCouponRequest {
    @Schema(description = "쿠폰 코드 (6자리)", example = "ZH1i6D")
    private String couponCode;
}
