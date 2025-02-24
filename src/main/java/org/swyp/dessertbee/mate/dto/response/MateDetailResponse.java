package org.swyp.dessertbee.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.dto.MatePlace;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
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
    private List<String> mateImage;
    private List<String> profileImage;
    private MatePlace place;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean saved;

    //디저트메이트 카테고리명
    private String mateCategory;

    public static MateDetailResponse fromEntity(Mate mate,
                                                List<String> mateImage,
                                                String category,
                                                UserEntity creator,
                                                List<String> profileImage,
                                                boolean saved){

        return MateDetailResponse.builder()
                .mateUuid(mate.getMateUuid())
                .storeId(mate.getStoreId())
                .userUuid(creator.getUserUuid())
                .nickname(creator.getNickname())
                .profileImage(profileImage)
                .title(mate.getTitle())
                .content(mate.getContent())
                .recruitYn(mate.getRecruitYn())
                .mateImage(mateImage)
                .mateCategory(category)
                .place(MatePlace.builder()
                        .placeName(mate.getPlaceName())
                        .longitude(mate.getLongitude())
                        .latitude(mate.getLatitude())
                        .address(mate.getAddress())
                        .build())
                .createdAt(mate.getCreatedAt())
                .updatedAt(mate.getUpdatedAt())
                .saved(saved)
                .build();
    }

}
