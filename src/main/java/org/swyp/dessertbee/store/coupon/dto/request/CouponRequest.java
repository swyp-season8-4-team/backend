package org.swyp.dessertbee.store.coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
public class CouponRequest {
    private String name;
    private LocalDateTime expiredAt;
    private UUID storeUuid;
}
