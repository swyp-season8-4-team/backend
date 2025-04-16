package org.swyp.dessertbee.statistics.store.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.store.entity.CouponUseLog;
import org.swyp.dessertbee.statistics.store.event.CouponUseEvent;
import org.swyp.dessertbee.statistics.store.repostiory.CouponUseLogRepository;
import org.swyp.dessertbee.statistics.common.exception.StoreLogExceptions.*;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUseLogEventHandler {

    private final CouponUseLogRepository couponUseLogRepository;

    @Async
    @Transactional
    public void handlCouponUseLogAction(CouponUseEvent event) {
        try {
            boolean isDuplicate = couponUseLogRepository.existsByCouponUuidAndUserUuid(
                    event.getCouponUuid(),
                    event.getUserUuid()
            );

            if (isDuplicate) {
                log.info("[쿠폰 로그] 중복 사용 감지 - 저장 생략 (couponUuid={}, userUuid={})", event.getCouponUuid(), event.getUserUuid());
                return;
            }

            couponUseLogRepository.save(
                    CouponUseLog.builder()
                            .storeId(event.getStoreId())
                            .userUuid(event.getUserUuid())
                            .usedAt(LocalDateTime.now())
                            .couponUuid(event.getCouponUuid())
                            .build()
            );
        } catch (Exception e) {
            log.warn("[쿠폰 사용 로그] 저장 실패: storeId={}, userUuid={}, couponUuid={}", event.getStoreId(), event.getUserUuid(), event.getCouponUuid(), e);
            throw new CouponUseLogCreateFailedException();
        }
    }
}