package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.statistics.common.RedisStatKeyBuilder;
import org.swyp.dessertbee.statistics.store.event.CouponUseEvent;
import org.swyp.dessertbee.statistics.common.exception.StoreStatisticsLogExceptions.*;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUseLogEventHandler {

    private final StringRedisTemplate redisTemplate;

    @Async
    @EventListener
    public void handleCouponUseLogAction(CouponUseEvent event) {
        try {
            String redisKey = String.format("stat:coupon:used:%s", event.getCouponUuid());
            Long added = redisTemplate.opsForSet().add(redisKey, event.getUserUuid().toString());

            if (added == 0L) {
                log.info("[쿠폰 로그] 중복 감지 - Redis 기준 생략 (couponUuid={}, userUuid={})", event.getCouponUuid(), event.getUserUuid());
                return;
            }

            redisTemplate.expire(redisKey, Duration.ofDays(3));

            // 시간별 통계 로그 저장
            String statKey = RedisStatKeyBuilder.build("coupon","used", event.getStoreId());
            redisTemplate.opsForValue().increment(statKey);
            redisTemplate.expire(statKey, Duration.ofDays(3));
        } catch (Exception e) {
            log.warn("[쿠폰 사용 로그] 저장 실패: storeId={}, userUuid={}, couponUuid={}", event.getStoreId(), event.getUserUuid(), event.getCouponUuid(), e);
            throw new CouponUseLogCreateFailedException();
        }
    }
}