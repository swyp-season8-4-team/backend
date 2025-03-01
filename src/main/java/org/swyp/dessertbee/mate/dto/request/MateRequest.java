package org.swyp.dessertbee.mate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateRequest {

    private UUID userUuid;

    private Long mateCategoryId;

}
