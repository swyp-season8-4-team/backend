package org.swyp.dessertbee.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.entity.Mate;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MateDetailResponse {

    private Long mateId;
    private Long userId;
    private String title;
    private String content;
    private Boolean recruitYn;
    private List<String> mateImage;

    //디저트메이트 카테고리명
    private String mateCategoryId;

    public static MateDetailResponse fromEntity(Mate mate,
                                                List<String> mateImage,
                                                String category){

        return MateDetailResponse.builder()
                .mateId(mate.getMateId())
                .userId(mate.getUserId())
                .title(mate.getTitle())
                .content(mate.getContent())
                .recruitYn(mate.getRecruitYn())
                .mateImage(mateImage)
                .mateCategoryId(category)
                .build();
    }

}
