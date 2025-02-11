package org.swyp.dessertbee.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDetailResponseDto extends UserResponseDto {
    private String email;    // 필수
    private String name;     // 선택
    private String phoneNumber;  // 선택
    private String address;      // 선택

    @Builder(builderMethodName = "detailBuilder")
    public UserDetailResponseDto(String userUuid, String nickname, UserEntity.Gender gender,
                                 Long imageId, List<Long> preferences, String mbti,
                                 String email, String name, String phoneNumber, String address) {
        super(userUuid, nickname, gender, imageId, preferences, mbti);
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}