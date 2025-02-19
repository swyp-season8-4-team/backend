package org.swyp.dessertbee.preference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponseDto {
    private Long id;
    private String preferenceName;
    private String preferenceDesc;
}