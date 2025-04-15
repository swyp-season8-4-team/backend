package org.swyp.dessertbee.user.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Getter
@Schema(description = "발급된 쿠폰 정보 응답")
public class IssuedCouponResponse {
    @Schema(description = "사용자 쿠폰 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userCouponId;

    @Schema(description = "쿠폰 이름", example = "아메리카노 증정 쿠폰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "쿠폰 코드", example = "ZH1i6D", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    @Schema(description = "QR 코드 이미지URL (Base64 인코딩)", example = "iVBORw0KGgoAAA...")
    private String qrImageUrl;

    @Schema(description = "사용 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isUsed;

    @Schema(description = "가게 이름", example = "카페 베네", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    @Schema(description = "쿠폰 만료일", example = "2025-05-01T23:59:59")
    private LocalDateTime expiryDate;
}
