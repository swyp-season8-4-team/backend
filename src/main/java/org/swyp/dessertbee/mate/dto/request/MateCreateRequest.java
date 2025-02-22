package org.swyp.dessertbee.mate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateCreateRequest {

    private Long mateId;

    @NotNull
    private UUID userUuid;

    @NotNull
    private Long userId;

    @NotNull
    private Long mateCategoryId;

    @NotNull
    private String title;

    @NotNull
    private String content;

    private Boolean recruitYn;


    private List<MultipartFile> mateImage; //모임 대표 이미지


}
