package org.swyp.dessertbee.mate.entity;

public enum MateApplyStatus {
        NONE,       // 신청 기록이 없음
        PENDING,    // 신청 중 (대기 상태)
        APPROVED,   // 승인됨
        REJECTED,   // 거절됨
        REAPPLY     // 재신청 중
}
