package org.swyp.dessertbee.mate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateCreateRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long mateCategoryId;

    @NotNull
    private String title;

    @NotNull
    private String content;

    private Boolean recruitYn;


    private List<String> mateImage; //모임 대표 이미지


}
