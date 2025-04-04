package org.swyp.dessertbee.community.mate.dto.request;

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
public class MateReplyCreateRequest {


    @NotNull
    private UUID userUuid;

    @NotNull
    private String content;

    private String report;


}
