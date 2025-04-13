package org.swyp.dessertbee.store.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

import java.time.LocalDateTime;

@Data
@Getter
@AllArgsConstructor
public class UserCouponDetailResponse {
    private Long userCouponId;
    private String qrImageUrl;
    private String storeName;
    private String couponName;
    private LocalDateTime expiryDate;
    private String couponCode;
    private CouponConditionType conditionType;
    private boolean isExpired;
}
