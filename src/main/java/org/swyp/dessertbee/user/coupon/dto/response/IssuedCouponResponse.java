package org.swyp.dessertbee.user.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Getter
public class IssuedCouponResponse {
    private Long userCouponId;
    private String couponName;
    private String couponCode;
    private String qrImageBase64;
    private boolean isUsed;
    private String storeName;
    private LocalDateTime expiryDate;
}
