package org.swyp.dessertbee.store.coupon.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UseCouponRequest {
    private String code;
}
