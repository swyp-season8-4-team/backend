package org.swyp.dessertbee.store.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.store.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.store.coupon.entity.UserCoupon;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;
import org.swyp.dessertbee.store.coupon.repository.UserCouponRepository;
import org.swyp.dessertbee.store.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.util.QRCodeGenerator;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;

    public IssuedCouponResponse issueCoupon(IssueCouponRequest request) {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다"));

        UserCoupon userCoupon = new UserCoupon(request.getUserId(), coupon);
        userCouponRepository.save(userCoupon);

        String qrBase64;
        try {
            byte[] qrBytes = QRCodeGenerator.generateQRCodeImage(userCoupon.getCode());
            qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            throw new RuntimeException("QR 코드 생성 중 오류 발생", e);
        }

        return new IssuedCouponResponse(
                userCoupon.getId(),
                coupon.getName(),
                coupon.getCode(),
                qrBase64,
                userCoupon.isUsed()
        );
    }

    public List<IssuedCouponResponse> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserId(userId).stream()
                .map(uc -> new IssuedCouponResponse(
                        uc.getId(),
                        uc.getCoupon().getName(),
                        uc.isUsed()
                ))
                .toList();
    }

    @Transactional
    public UsedCouponResponse useCouponByCode(UseCouponRequest request) {
        UserCoupon userCoupon = userCouponRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));

        if (userCoupon.isUsed()) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        userCoupon.use(); // 사용 처리

        return new UsedCouponResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getCode(),
                userCoupon.isUsed()
        );
    }
}
