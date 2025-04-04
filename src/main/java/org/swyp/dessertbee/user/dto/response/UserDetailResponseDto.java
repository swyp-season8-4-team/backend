package org.swyp.dessertbee.user.dto.response;

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
    private Boolean isPreferencesSet;  // 선호도 설정 여부

    @Builder(builderMethodName = "detailBuilder")
    public UserDetailResponseDto(String userUuid, String nickname, UserEntity.Gender gender,
                                 String profileImage, List<Long> preferences, String mbti,
                                 String email, String name, String phoneNumber, String address,
                                 Boolean isPreferencesSet) {
        super(userUuid, nickname, gender, profileImage, preferences, mbti);
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isPreferencesSet = isPreferencesSet;
    }
}