package org.swyp.dessertbee.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
