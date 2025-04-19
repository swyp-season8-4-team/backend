package org.swyp.dessertbee.user.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.statistics.store.event.CouponUseEvent;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.*;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.user.coupon.entity.UserCoupon;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;
import org.swyp.dessertbee.user.coupon.repository.UserCouponRepository;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.user.coupon.util.CouponCodeGenerator;
import org.swyp.dessertbee.user.coupon.util.QRCodeGenerator;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCouponServiceImpl implements UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final CouponCodeGenerator couponCodeGenerator;
    private final UserService userService;

    /**
     * 쿠폰 발급
     */
    public IssuedCouponResponse issueCoupon(IssueCouponRequest request, UserEntity user) {
        Coupon coupon = couponRepository.findByCouponUuidForUpdate(request.getCouponUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        // 한 ID당 하나의 쿠폰 발급
        boolean alreadyIssued = userCouponRepository.existsByUserAndCoupon(user, coupon);
        if (alreadyIssued) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        // 수량 부족 체크
        if (coupon.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
        String uniqueCouponCode = couponCodeGenerator.generateUniqueCouponCode();

        String qrBase64;
        try {
            byte[] qrBytes = QRCodeGenerator.generateQRCodeImage(uniqueCouponCode);
            qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.QR_GENERATION_FAILED);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));

        if (!userCoupon.getUser().getUserUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.USER_COUPON_FORBIDDEN);
        }

        Coupon coupon = userCoupon.getCoupon();
        Store store = coupon.getStore();

        return new UserCouponDetailResponse(
                userCoupon.getId(),
                userCoupon.getQrImageUrl(),
                coupon.getStore().getName(),
                coupon.getName(),
                coupon.getExpiryDate(),
                userCoupon.getCouponCode(),
                coupon.getConditionType(),
                userCoupon.isExpired(),
                store.getStoreUuid(),
                coupon.getCouponUuid()
        );
    }


    /**
     * 쿠폰 사용
     */
    @Transactional
    public UsedCouponResponse useCouponByCode(UseCouponRequest request) {
        UserCoupon userCoupon = userCouponRepository.findByCouponCode(request.getCouponCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));

        // 쿠폰이 속한 가게의 UUID와 요청한 storeUuid가 일치하는지 검증
        UUID couponStoreUuid = userCoupon.getCoupon().getStore().getStoreUuid();
        if (!couponStoreUuid.equals(request.getStoreUuid())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS); // 혹은 적절한 에러코드 사용
        }

        UserEntity user = userService.getCurrentUser();
      
        if (userCoupon.isUsed()) {
            throw new BusinessException(ErrorCode.ALREADY_USED_COUPON);
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

    /**
     * 특정 가게에서 생성한 모든 쿠폰들에 대해 사용자가 발급받았는지 여부 조회
     */
    public List<CouponIssuedStatusResponse> getCouponIssuedStatusByStore(UUID storeUuid, UUID userUuid) {
        // 가게의 모든 쿠폰 조회
        List<Coupon> storeCoupons = couponRepository.findAllByStore_StoreUuid(storeUuid);

        // 사용자가 발급받은 해당 가게 쿠폰 ID 목록 조회
        List<Long> issuedCouponIds = userCouponRepository.findAllByUser_UserUuidAndCoupon_Store_StoreUuid(userUuid, storeUuid)
                .stream()
                .map(uc -> uc.getCoupon().getId())
                .collect(Collectors.toList());


        return storeCoupons.stream()
                .map(coupon -> new CouponIssuedStatusResponse(
                        coupon.getId(),
                        coupon.getName(),
                        issuedCouponIds.contains(coupon.getId())
                ))
                .collect(Collectors.toList());
    }

}
