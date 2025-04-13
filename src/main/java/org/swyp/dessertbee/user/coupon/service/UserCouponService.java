package org.swyp.dessertbee.user.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.user.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.user.coupon.entity.UserCoupon;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;
import org.swyp.dessertbee.user.coupon.repository.UserCouponRepository;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.user.coupon.util.CouponCodeGenerator;
import org.swyp.dessertbee.user.coupon.util.QRCodeGenerator;
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

    /**
     * 쿠폰 발급
     */
    public IssuedCouponResponse issueCoupon(IssueCouponRequest request, UserEntity user) {
        Coupon coupon = couponRepository.findByCouponUuid(request.getCouponUuid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 한 ID당 하나의 쿠폰 발급
        boolean alreadyIssued = userCouponRepository.existsByUserAndCoupon(user, coupon);
        if (alreadyIssued) {
            throw new IllegalStateException("이미 해당 쿠폰을 발급받은 사용자입니다.");
        }

        String uniqueCouponCode = couponCodeGenerator.generateUniqueCouponCode();

        String qrBase64;
        try {
            byte[] qrBytes = QRCodeGenerator.generateQRCodeImage(uniqueCouponCode);
            qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            throw new RuntimeException("QR 코드 생성 중 오류 발생", e);
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .couponCode(uniqueCouponCode)
                .qrImageUrl(qrBase64) // QR 이미지 저장
                .build();

        userCouponRepository.save(userCoupon);
        //수량 감소
        coupon.decreaseQuantity();

        return new IssuedCouponResponse(
                userCoupon.getId(),
                coupon.getName(),
                userCoupon.getCouponCode(),
                qrBase64,
                userCoupon.isUsed(),
                coupon.getStore().getName(),
                coupon.getExpiryDate()
        );
    }

    /**
     * 발급받은 쿠폰 목록 조회
     */
    public List<IssuedCouponResponse> getUserCoupons(UUID userUuid) {
        return userCouponRepository.findAllByUser_UserUuid(userUuid).stream()
                .map(uc -> {
                    Coupon coupon = uc.getCoupon();
                    return new IssuedCouponResponse(
                            uc.getId(),
                            coupon.getName(),
                            uc.getCouponCode(),
                            null, // QR 생략
                            uc.isUsed(),
                            coupon.getStore().getName(),
                            coupon.getExpiryDate()
                    );
                })
                .toList();
    }

    /**
     * 쿠폰 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public UserCouponDetailResponse getUserCouponDetail(Long userCouponId, UUID userUuid) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));

        if (!userCoupon.getUser().getUserUuid().equals(userUuid)) {
            throw new IllegalArgumentException("본인의 쿠폰만 조회할 수 있습니다.");
        }

        Coupon coupon = userCoupon.getCoupon();

        return new UserCouponDetailResponse(
                userCoupon.getId(),
                userCoupon.getQrImageUrl(),
                coupon.getStore().getName(),
                coupon.getName(),
                coupon.getExpiryDate(),
                userCoupon.getCouponCode(),
                coupon.getConditionType(),
                userCoupon.isExpired()
        );
    }


    /**
     * 쿠폰 사용
     */
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

    /**
     * 쿠폰 사용 현황
     */

    public CouponUsageStatusResponse getCouponUsageStats(UUID userUuid) {
        List<UserCoupon> userCoupons = userCouponRepository.findAllByUser_UserUuid(userUuid);

        long expiredCount = 0;
        long usedCount = 0;
        long unusedCount = 0;

        for (UserCoupon userCoupon : userCoupons) {
            if (userCoupon.isExpired()) {
                expiredCount++;
            } else {
                if (userCoupon.isUsed()) {
                    usedCount++;
                } else {
                    unusedCount++;
                }
            }
        }

        return new CouponUsageStatusResponse(usedCount, unusedCount, expiredCount);
    }
}
