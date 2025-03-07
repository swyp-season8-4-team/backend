package org.swyp.dessertbee.community.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.community.review.dto.ReviewContentDto;
import org.swyp.dessertbee.community.review.dto.ReviewPlace;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {


    @NotNull
    private UUID userUuid;

    @NotNull
    private Long reviewCategoryId;

    @NotNull
    private String title;


    private ReviewPlace place;

    private List<Long> deleteImageIds;

    private List<MultipartFile> reviewImages;

    @NotNull
    private List<ReviewContentDto> contents;

}
