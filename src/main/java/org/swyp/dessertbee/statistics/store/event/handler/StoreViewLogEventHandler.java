package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.statistics.common.RedisStatKeyBuilder;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;
import org.swyp.dessertbee.statistics.store.event.StoreViewEvent;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreViewLogEventHandler {

    private final StringRedisTemplate redisTemplate;

    @Async
    @EventListener
    public void handleStoreViewAction(StoreViewEvent event) {
        try {
            String redisKey = RedisStatKeyBuilder.build("view", "store", event.getStoreId());
            redisTemplate.opsForValue().increment(redisKey);
            redisTemplate.expire(redisKey, Duration.ofDays(3));
        } catch (Exception e) {
            log.warn("[가게 상세 조회 로그] 저장 실패: storeId={}, userUuid={}", event.getStoreId(), event.getUserUuid(), e);
            throw new StoreViewLogFailedException();
        }
    }
}