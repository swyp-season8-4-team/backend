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
    @Schema(description = "사용자 쿠폰 ID", example = "1")
    private Long userCouponId;

    @Schema(description = "쿠폰 이름", example = "아메리카노 증정 쿠폰")
    private String couponName;

    @Schema(description = "쿠폰 코드", example = "ZH1i6D")
    private String couponCode;

    @Schema(description = "QR 코드 이미지URL (Base64 인코딩)", example = "iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAAAzUlEQVR4Xu2S6w3EIAyD2ZyM1s16fgA9VbpbwHElQ+KvfyzG/V/XeG9easBqwGrAasDKAmpIU6cdZyCgAV5T9uzSgGJByMb8mpIBEA3oxi8WOF47igSGNE9NmvKALUUKtrKAQkesSRee3MmzAM1KfDBMBPBQuJK7tf1wsgBmWBQp7QklAgpMLcZ5HiCxHzHITWQBrMgtITj3QEAjXPnkD6EAV3BmciyEpAKuaKWxAG+aFpgHPNhdREOBISng3p4H/FYDVgNWA1YDVgPW9QFq2OuUw3JTZQAAAABJRU5ErkJggg==")
    private String qrImageBase64;

    @Schema(description = "사용 여부", example = "false")
    private boolean isUsed;

    @Schema(description = "가게 이름", example = "카페 베네")
    private String storeName;

    @Schema(description = "쿠폰 만료일", example = "2025-05-01T23:59:59")
    private LocalDateTime expiryDate;
}
