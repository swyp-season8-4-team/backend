package org.swyp.dessertbee.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2CallbackRequest {
    @NotBlank(message = "Provider cannot be empty")
    private String provider;  // OAuth2 제공자
    @NotBlank(message = "Authorization code cannot be empty")
    private String code;      // 인증 코드
}
