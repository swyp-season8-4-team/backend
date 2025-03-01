package org.swyp.dessertbee.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateRequestDto {
    private String nickname;
    private List<Long> preferences;
    private String name;
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$|^01(?:0|1|[6-9])-\\d{4}-\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;
    private String address;
    private UserEntity.Gender gender;
    private String mbti;
    private Boolean removeProfileImage;  // 프로필 이미지 제거 플래그 추가

    @Builder
    public UserUpdateRequestDto(String nickname, List<Long> preferences, String name,
                                String phoneNumber, String address, UserEntity.Gender gender,
                                String mbti, Boolean removeProfileImage) {
        this.nickname = nickname;
        this.preferences = preferences;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.mbti = mbti;
        this.removeProfileImage = removeProfileImage;
    }
}
