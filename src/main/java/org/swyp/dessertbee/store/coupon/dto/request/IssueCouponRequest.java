package org.swyp.dessertbee.store.coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
@Getter
public class IssueCouponRequest {
   private Long userId;
   private Long couponId;
}
