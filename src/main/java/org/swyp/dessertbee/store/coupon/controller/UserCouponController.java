package org.swyp.dessertbee.store.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.security.CustomUserDetails;
import org.swyp.dessertbee.store.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.store.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.store.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.store.coupon.service.UserCouponService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class UserCouponController {


    private final UserCouponService userCouponService;
    private final UserRepository userRepository;


    /**
     * 쿠폰 발급
     */
    @PostMapping("/issue")
    public ResponseEntity<IssuedCouponResponse> issueCoupon(
            @RequestBody IssueCouponRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
        return ResponseEntity.ok(userCouponService.issueCoupon(request, user));
    }


    /**
     * 발급 받은 쿠폰 목록 조회
     */
    @GetMapping("/myCoupon")
    public ResponseEntity<List<IssuedCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        return ResponseEntity.ok(userCouponService.getUserCoupons(userUuid));
    }

    /**
     * 발급 받은 쿠폰 상세 조회
     */
    @GetMapping("/myCoupon/{userCouponId}")
    public ResponseEntity<UserCouponDetailResponse> getUserCouponDetail(
            @PathVariable Long userCouponId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        UserCouponDetailResponse response = userCouponService.getUserCouponDetail(userCouponId, userUuid);
        return ResponseEntity.ok(response);
    }
    /**
     * 쿠폰 사용 현황 조회
     */
    @GetMapping("/status-count")
    public ResponseEntity<CouponUsageStatusResponse> getCouponStatusCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();

        CouponUsageStatusResponse response = userCouponService.getCouponUsageStats(userUuid);
        return ResponseEntity.ok(response);
    }
}
