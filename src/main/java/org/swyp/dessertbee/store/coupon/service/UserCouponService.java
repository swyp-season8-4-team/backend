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
import org.swyp.dessertbee.store.coupon.util.CouponCodeGenerator;
import org.swyp.dessertbee.store.coupon.util.QRCodeGenerator;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final CouponCodeGenerator couponCodeGenerator;

    public IssuedCouponResponse issueCoupon(IssueCouponRequest request, UserEntity user) {
        Coupon coupon = couponRepository.findByCouponUuid(request.getCouponUuid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 한 ID당 하나의 쿠폰 발급
        boolean alreadyIssued = userCouponRepository.existsByUserAndCoupon(user, coupon);
        if (alreadyIssued) {
            throw new IllegalStateException("이미 해당 쿠폰을 발급받은 사용자입니다.");
        }

        String uniqueCouponCode = couponCodeGenerator.generateUniqueCouponCode();

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .couponCode(uniqueCouponCode)
                .build();

        userCouponRepository.save(userCoupon);

        String qrBase64;
        try {
            byte[] qrBytes = QRCodeGenerator.generateQRCodeImage(userCoupon.getCouponCode());
            qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            throw new RuntimeException("QR 코드 생성 중 오류 발생", e);
        }

        return new IssuedCouponResponse(
                userCoupon.getId(),
                coupon.getName(),
                userCoupon.getCouponCode(),
                qrBase64,
                userCoupon.isUsed()
        );
    }

    public List<IssuedCouponResponse> getUserCoupons(UUID userUuid) {
        return userCouponRepository.findAllByUser_UserUuid(userUuid).stream()
                .map(uc -> new IssuedCouponResponse(
                        uc.getId(),
                        uc.getCoupon().getName(),
                        uc.getCouponCode(),
                        null, // QR 생략
                        uc.isUsed()
                ))
                .toList();
    }

    @Transactional
    public UsedCouponResponse useCouponByCode(UseCouponRequest request) {
        UserCoupon userCoupon = userCouponRepository.findByCouponCode(request.getCouponCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));

        if (userCoupon.isUsed()) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        userCoupon.use(); // 사용 처리

        return new UsedCouponResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getCouponCode(),
                userCoupon.isUsed()
        );
    }
}
