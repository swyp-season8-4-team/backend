package org.swyp.dessertbee.user.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
@Schema(description = "쿠폰 발급 요청")
public class IssueCouponRequest {
   @Schema(description = "사용자 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
   private UUID userUuid;

   @Schema(description = "쿠폰 UUID", example = "550e8400-e29b-41d4-a716-446655440001")
   private UUID couponUuid;
}
