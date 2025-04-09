package org.swyp.dessertbee.store.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
@Getter
public class UsedCouponResponse {

    Long userCouponId;
    String couponName;
    String userCouponCode;
    boolean used;

}
