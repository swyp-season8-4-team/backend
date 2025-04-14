package org.swyp.dessertbee.user.coupon.scheduler;

import lombok.RequiredArgsConstructor;
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
public class CouponScheduler {

    private final CouponRepository couponRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void expireCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findAllByHasExpiryDateIsTrueAndStatusIs(CouponStatus.CREATED)
                .stream()
                .filter(coupon -> coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now()))
                .toList();

        expiredCoupons.forEach(Coupon::expire);
    }
}
