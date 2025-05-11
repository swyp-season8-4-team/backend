package org.swyp.dessertbee.store.coupon.dto.request.couponType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;

@Schema(description = "증정 쿠폰 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftCouponRequest implements CouponTypeRequest {

    @Schema(description = "증정 메뉴명", example = "아메리카노")
    @NotBlank
    private String giftMenuName;

    @Override
    @NotNull
    public CouponType getType() {
        return CouponType.GIFT;
    }
}
