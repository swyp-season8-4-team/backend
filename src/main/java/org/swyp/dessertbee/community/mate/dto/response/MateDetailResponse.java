package org.swyp.dessertbee.community.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.mate.dto.MatePlace;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateDetailResponse {

    private UUID mateUuid;
    private Long storeId;
    private UUID userUuid;
    private String nickname;
    private String title;
    private String content;
    private Boolean recruitYn;
    private String mateImage;
    private String profileImage;
    private MatePlace place;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean saved;
    private MateApplyStatus applyStatus;
    private UserEntity.Gender gender;

    //디저트메이트 카테고리명
    private String mateCategory;

    public static MateDetailResponse fromEntity(Mate mate,
                                                String mateImage,
                                                String category,
                                                UserEntity creator,
                                                String profileImage,
                                                boolean saved,
                                                MateApplyStatus applyStatus,
                                                Store store) {

        return MateDetailResponse.builder()
                .mateUuid(mate.getMateUuid())
                .storeId(mate.getStoreId())
                .userUuid(creator.getUserUuid())
                .nickname(creator.getNickname())
                .gender(creator.getGender())
                .profileImage(profileImage)
                .title(mate.getTitle())
                .content(mate.getContent())
                .recruitYn(mate.getRecruitYn())
                .mateImage(mateImage)
                .mateCategory(category)
                .place(MatePlace.builder()
                        .placeName(store != null ? store.getName() : null)
                        .longitude(store != null ? store.getLongitude() : null)
                        .latitude(store != null ? store.getLatitude() : null)
                        .address(store != null ? store.getAddress() : null)
                        .build())
                .createdAt(mate.getCreatedAt())
                .updatedAt(mate.getUpdatedAt())
                .saved(saved)
                .applyStatus(applyStatus)
                .build();
    }


}
