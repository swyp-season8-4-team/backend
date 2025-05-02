package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreImageResponse {

    @Schema(description = "이미지 ID", example = "123")
    private Long id;

    @Schema(description = "이미지 URL", example = "image.jpg")
    private String url;
}

