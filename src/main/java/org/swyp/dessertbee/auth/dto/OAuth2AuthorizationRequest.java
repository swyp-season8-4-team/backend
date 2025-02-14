package org.swyp.dessertbee.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class OAuth2AuthorizationRequest {
    @NotBlank(message = "Provider cannot be empty")
    private String provider;  // OAuth2 제공자
}
