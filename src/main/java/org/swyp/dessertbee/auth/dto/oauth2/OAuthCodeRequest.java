package org.swyp.dessertbee.auth.dto.oauth2;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프론트엔드에서 전달받은 OAuth 인가 코드를 담는 DTO
 */
@Getter
@NoArgsConstructor
public class OAuthCodeRequest {
    private String code;        // OAuth 인가 코드
    private String provider;    // OAuth 제공자
}