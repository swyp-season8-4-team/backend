package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.statistics.common.RedisStatKeyBuilder;
import org.swyp.dessertbee.statistics.common.exception.StoreStatisticsLogExceptions.*;
import org.swyp.dessertbee.statistics.store.event.StoreSaveActionEvent;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreSaveLogEventHandler {

    private final StringRedisTemplate redisTemplate;

    @Async
    @EventListener
    public void handleStoreSaveAction(StoreSaveActionEvent event) {
        try {
            String statKey = RedisStatKeyBuilder.build("save", "store", event.getStoreId());

            // 저장/해제 구분
            long delta = "SAVE".equals(event.getAction().name()) ? 1 : -1;

            redisTemplate.opsForValue().increment(statKey, delta);
            redisTemplate.expire(statKey, Duration.ofDays(3));
        } catch (Exception e) {
            log.warn("[가게 저장 관련 로그] 저장 실패: storeId={}, userUuid={}, action={}", event.getStoreId(), event.getUserUuid(), event.getAction(), e);
            throw new StoreSaveLogCreateFailedException();
        }
    }
}