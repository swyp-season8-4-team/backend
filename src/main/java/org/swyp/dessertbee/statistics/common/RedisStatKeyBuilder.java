package org.swyp.dessertbee.statistics.common;

import java.time.LocalDateTime;

public class RedisStatKeyBuilder {
    public static String build(String actionType, String category, Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("stat:%s:%s:%s:%d:%02d", actionType, category, now.toLocalDate(), storeId, now.getHour());
    }
}