package org.swyp.dessertbee.community.mate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.community.mate.dto.MatePlace;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateCreateRequest {

    @NotBlank(message = "작성하는 사람의 uuid를 넘겨주세요.")
    @Schema(description = "작성하는 사람 uuid")
    private UUID userUuid;

    @NotBlank(message = "디저트메이트 카테고리 선택해주세요.")
    @Schema(description = "디저트메이트 카테고리", example = "1")
    private Long mateCategoryId;

    @NotBlank(message = "디저트메이트 수용 인원을 선택해주세요.")
    @Schema(description = "디저트메이트 수용 인원", example = "2")
    private Long capacity;

    @NotBlank(message = "디저트메이트 제목을 작성해주세요.")
    @Schema(description = "디저트메이트 제목 작성", example = "저랑 같이 홍대 빵지순례할 사람 찾습니다.")
    private String title;

    @NotBlank(message = "디저트메이트 내용을 작성해주세요.")
    @Schema(description = "디저트메이트 내용 작성", example = "홍대 빵지순례 리스트 200개 있습니다. 같이 맛있는거 먹으러 가요.")
    private String content;

    @NotBlank(message = "디저트메이트 모집 여부를 선택해주세요.")
    @Schema(description = "디저트메이트 모집 여부", example = "true")
    private Boolean recruitYn;

    @Schema(description = "디저트메이트 지정 장소", example = " placeName : 모코모코, address : 서울 마포구 와우산로29길 47 1층, latitude: 37.55564710, longitude : 126.92734908")
    private MatePlace place;

    @Schema(description = "디저트메이트 이미지", example = " mateImage=: https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/mate/75/7edd7706-0bfa-46cf-a6c2-ad67f8a9a440-IMG_8828.jpeg")
    private MultipartFile mateImage; //모임 대표 이미지



}
