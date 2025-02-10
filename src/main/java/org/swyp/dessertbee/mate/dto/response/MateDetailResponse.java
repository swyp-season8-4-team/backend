package org.swyp.dessertbee.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.entity.Mate;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateDetailResponse {

    private UUID mateUuid;
    private UUID userUuid;
    private String title;
    private String content;
    private Boolean recruitYn;
    private List<String> mateImage;

    //디저트메이트 카테고리명
    private String mateCategory;

    public static MateDetailResponse fromEntity(Mate mate,
                                                List<String> mateImage,
                                                String category,
                                                UUID userUuid){

        return MateDetailResponse.builder()
                .mateUuid(mate.getMateUuid())
                .userUuid(userUuid)
                .title(mate.getTitle())
                .content(mate.getContent())
                .recruitYn(mate.getRecruitYn())
                .mateImage(mateImage)
                .mateCategory(category)
                .build();
    }

}
