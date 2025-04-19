package org.swyp.dessertbee.user.coupon.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class CouponIssuedStatusResponse {
    private Long couponId;
    private String couponName;
    private boolean issued;
}
