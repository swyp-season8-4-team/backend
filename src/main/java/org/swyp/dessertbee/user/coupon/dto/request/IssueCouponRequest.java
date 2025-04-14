package org.swyp.dessertbee.user.coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
public class IssueCouponRequest {
   private UUID userUuid;
   private UUID couponUuid;
}
