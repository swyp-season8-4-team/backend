package org.swyp.dessertbee.store.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 정기 휴무 유형 (매주 또는 매월)
 */
@Schema(description = "정기 휴무 유형 (WEEKLY=매주, MONTHLY=매월, NONE=없음)")
@Getter
@AllArgsConstructor
public enum RegularClosureType {
    WEEKLY("매주"),     // 매주 특정 요일
    MONTHLY("매월"),    // 매월 특정 주차의 특정 요일
    NONE("없음");       // 정기 휴무 없음

    private final String description;
}