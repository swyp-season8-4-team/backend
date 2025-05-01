package org.swyp.dessertbee.user.coupon.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponStatus;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponRepository couponRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정 실행
    public void expireCoupons() {
        log.info("[스케줄러 시작] 쿠폰 만료 체크 at {}", LocalDateTime.now());
        List<Coupon> expiredCoupons = couponRepository.findAllByHasExpiryDateIsTrueAndStatusIs(CouponStatus.CREATED)
                .stream()
                .filter(coupon -> coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now()))
                .toList();

        expiredCoupons.forEach(Coupon::expire);
        log.info("[스케줄러 종료] 쿠폰 만료 체크 완료 at {}", LocalDateTime.now());
    }
}