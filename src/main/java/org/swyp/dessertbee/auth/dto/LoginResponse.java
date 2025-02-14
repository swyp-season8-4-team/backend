package org.swyp.dessertbee.auth.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private UUID userUuid;
    private String email;
    private String nickname;
}
