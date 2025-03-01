package org.swyp.dessertbee.user.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {
    private String userUuid;  // 필수
    private String nickname;  // 필수
    private UserEntity.Gender gender;    // 선택
    private String profileImageUrl;  // 선택
    private List<Long> preferences;  // 선택
    private String mbti;     // 선택

    @Builder
    public UserResponseDto(String userUuid, String nickname, UserEntity.Gender gender,
                           String profileImageUrl, List<Long> preferences, String mbti) {
        this.userUuid = userUuid;
        this.nickname = nickname;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.preferences = preferences;
        this.mbti = mbti;
    }
}
