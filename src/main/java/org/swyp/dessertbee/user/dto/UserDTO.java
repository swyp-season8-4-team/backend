package org.swyp.dessertbee.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private String email;
    private String nickname;
    private List<String> roles;
}
