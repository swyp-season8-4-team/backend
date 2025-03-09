package org.swyp.dessertbee.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularSearchResponse {
    private String keyword;
    private int rank;
}
