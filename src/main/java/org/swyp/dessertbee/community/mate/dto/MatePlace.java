package org.swyp.dessertbee.community.mate.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatePlace {

    private String placeName;

    private String address;

    private BigDecimal  latitude;
    private BigDecimal  longitude;

}
