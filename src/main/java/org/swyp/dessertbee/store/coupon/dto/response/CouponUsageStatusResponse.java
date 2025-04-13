package org.swyp.dessertbee.store.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class CouponUsageStatusResponse {
    private long usedCount;
    private long unusedCount;
    private long expiredCount;
}
