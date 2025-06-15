package org.swyp.dessertbee.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프론트엔드에서 전달받은 OAuth 인가 코드를 담는 DTO
 */
@Getter
@NoArgsConstructor
public class OAuthCodeRequest {
    @Schema(
            description = "OAuth 제공자로부터 받은 인가 코드",
            example = "4/0AfJohXn...hcGvDEgFKixQ",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;        // OAuth 인가 코드

    @Schema(
            description = "OAuth 제공자 (예: GOOGLE, KAKAO, NAVER)",
            example = "KAKAO",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String provider;    // OAuth 제공자
}