package org.swyp.dessertbee.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MBTI 정보를 전달하기 위한 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbtiResponseDto {

    private Long id;
    private String mbtiType;
    private String mbtiName;
    private String mbtiDesc;
}