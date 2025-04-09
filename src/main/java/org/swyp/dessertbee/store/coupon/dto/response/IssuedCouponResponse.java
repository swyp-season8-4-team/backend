package org.swyp.dessertbee.store.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
@Getter
public class IssuedCouponResponse {
    Long userCouponId;
    String couponName;
    String couponCode;
    String qrImageBase64;
    boolean isUsed;

    public IssuedCouponResponse(Long userCouponId, String couponName, String couponCode, String qrImageBase64) {
        this.userCouponId = userCouponId;
        this.couponName = couponName;
        this.couponCode = couponCode;
        this.qrImageBase64 = qrImageBase64;
        this.isUsed = false;
    }

    public IssuedCouponResponse(Long userCouponId, String couponName,  boolean isUsed) {
        this.userCouponId = userCouponId;
        this.couponName = couponName;
        this.isUsed = isUsed;
    }
}
