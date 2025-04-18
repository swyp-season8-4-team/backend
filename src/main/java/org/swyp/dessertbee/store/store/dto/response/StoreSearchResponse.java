package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSearchResponse {

    @Schema(description = "가게 고유 ID", example = "1")
    private Long storeId;

    @Schema(description = "가게 UUID", example = "58fbeb5e-ff24-41e6-8460-301b1a424e53")
    private UUID storeUuid;

    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @Schema(description = "가게 주소", example = "서울 마포구 양화로 23길 8")
    private String address;

    @Schema(description = "가게 썸네일 이미지 URL", example = "https://dessertbee.s3.ap-northeast-2.amazonaws.com/store/3/example.png")
    private String thumbnail;
}