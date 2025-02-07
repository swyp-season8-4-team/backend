package org.swyp.dessertbee.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private Long imageId;
    private UUID userUuid;
    private String email;
    private String nickname;
    private String name;
    private String preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String phoneNumber;
    private String address;
    private UserEntity.Gender gender;
    private List<String> roles;

    public static UserDTO fromEntity(UserEntity entity, List<String> roles) {
        return UserDTO.builder()
                .id(entity.getId())
                .imageId(entity.getImageId())
                .userUuid(entity.getUserUuid())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .name(entity.getName())
                .preferences(entity.getPreferences())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .phoneNumber(entity.getPhoneNumber())
                .address(entity.getAddress())
                .gender(entity.getGender())
                .roles(roles)
                .build();
    }

    // 프로필 수정용 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String nickname;
        private String name;
        private String phoneNumber;
        private String address;
        private String preferences;
        private UserEntity.Gender gender;
    }
}