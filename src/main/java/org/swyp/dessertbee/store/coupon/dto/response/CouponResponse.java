package org.swyp.dessertbee.store.coupon.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.CouponStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Getter
public class CouponResponse {

    private String name;
    private String code;
    private CouponStatus status; //ISSUED, USED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    public CouponResponse(Coupon coupon) {
        this.name = coupon.getName();
        this.status = coupon.getStatus();
        this.createdAt = coupon.getCreatedAt();
        this.expiredAt = coupon.getExpiredAt();
    }
}
