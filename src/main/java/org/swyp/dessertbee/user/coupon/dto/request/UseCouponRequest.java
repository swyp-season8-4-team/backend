package org.swyp.dessertbee.user.coupon.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UseCouponRequest {
    private String couponCode;
}
