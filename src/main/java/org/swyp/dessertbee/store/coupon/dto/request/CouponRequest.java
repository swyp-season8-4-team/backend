package org.swyp.dessertbee.store.coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Getter
public class CouponRequest {
    private String name;
    private LocalDateTime expiredAt;
}
