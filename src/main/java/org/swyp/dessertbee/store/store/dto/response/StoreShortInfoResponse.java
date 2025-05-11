package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StoreShortInfoResponse {
    @Schema(description = "가게 ID", example = "1")
    private Long storeId;

    @Schema(description = "가게 UUID", example = "57422273-052e-45ea-b6d6-20c4411bf8b7")
    private UUID storeUuid;

    @Schema(description = "가게 이름", example = "카스테라 연구소")
    private String name;
}
