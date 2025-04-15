package org.swyp.dessertbee.store.coupon.dto.response.couponType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "증정 쿠폰 응답")
public class GiftCouponResponse implements CouponTypeResponse {

    @Schema(description = "쿠폰 타입", example = "GIFT")
    private CouponType type;

    @Schema(description = "증정 메뉴 이름", example = "아메리카노")
    private String giftMenuName;
}
