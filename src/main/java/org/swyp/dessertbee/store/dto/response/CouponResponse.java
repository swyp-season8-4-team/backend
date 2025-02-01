package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.entity.Coupon;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class CouponResponse {
    private String title;
    private String description;
    private LocalDate expiryDate;

    public static CouponResponse fromEntity(Coupon coupon) {
        return new CouponResponse(coupon.getTitle(), coupon.getDescription(), coupon.getExpiryDate());
    }
}
