package org.swyp.dessertbee.user.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;

import java.time.LocalDateTime;

@Data
@Getter
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 상세 응답")
public class UserCouponDetailResponse {
    @Schema(description = "사용자 쿠폰 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userCouponId;

    @Schema(description = "QR 이미지 URL", example = "iVBORw0KGgoAAA...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String qrImageUrl;

    @Schema(description = "가게 이름", example = "스타벅스", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "쿠폰 이름", example = "아메리카노 증정 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "쿠폰 만료일", example = "2025-05-01T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "쿠폰 코드", example = "ZH1i6D", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    @Schema(description = "조건 타입", example = "AMOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
    private CouponConditionType conditionType;

    @Schema(description = "만료 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isExpired;
}
