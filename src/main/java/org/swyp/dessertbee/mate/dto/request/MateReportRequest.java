package org.swyp.dessertbee.mate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateReportRequest {

    @NotNull
    private UUID userUuid;

    @NotNull
    private Long reportCategoryId;

    private String reportComment;
}
