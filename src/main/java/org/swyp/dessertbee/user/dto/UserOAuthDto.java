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
public class UserOAuthDto {
    private Long id;
    private Long imageId;
    private UUID userUuid;
    private String email;
    private String nickname;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String phoneNumber;
    private String address;
    private UserEntity.Gender gender;
    private List<String> roles;

    public static UserOAuthDto fromEntity(UserEntity entity, List<String> roles) {
        return UserOAuthDto.builder()
                .id(entity.getId())
                .imageId(entity.getImageId())
                .userUuid(entity.getUserUuid())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .phoneNumber(entity.getPhoneNumber())
                .address(entity.getAddress())
                .gender(entity.getGender())
                .roles(roles)
                .build();
    }
}