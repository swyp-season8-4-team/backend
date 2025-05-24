package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.statistics.common.RedisStatKeyBuilder;
import org.swyp.dessertbee.statistics.store.event.CommunityReviewActionEvent;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityReviewLogEventHandler {

    private final StringRedisTemplate redisTemplate;

    @Async
    @EventListener
    public void handleCommunityReviewLogAction(CommunityReviewActionEvent event) {
        try {
            String statKey = RedisStatKeyBuilder.build("review", "comm", event.getStoreId());

            long delta = "CREATE".equals(event.getAction().name()) ? 1 : -1;

            redisTemplate.opsForValue().increment(statKey, delta);
            redisTemplate.expire(statKey, Duration.ofDays(3));
        } catch (Exception e) {
            log.warn("[커뮤니티 리뷰 로그] 저장 실패: storeId={}, reviewId={}, userUuid={}, action={}", event.getStoreId(), event.getReviewId(), event.getUserUuid(), event.getAction(), e);
            throw new CommunityReviewLogCreateFailedException();
        }
    }
}