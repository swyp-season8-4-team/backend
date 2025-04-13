package org.swyp.dessertbee.user.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.security.CustomUserDetails;
import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.user.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.user.coupon.service.UserCouponService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "UserCoupon", description = "사용자 쿠폰 관련 API")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCouponController {


    private final UserCouponService userCouponService;
    private final UserRepository userRepository;


    /**
     * 쿠폰 발급
     */
    @Operation(summary = "쿠폰 발급")
    @PostMapping("/coupons/issue")
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
    @Operation(summary = "발급받은 쿠폰 목록 조회")
    @GetMapping("/coupons/my")
    public ResponseEntity<List<IssuedCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        return ResponseEntity.ok(userCouponService.getUserCoupons(userUuid));
    }

    /**
     * 발급 받은 쿠폰 상세 조회
     */
    @Operation(summary = "발급 받은 쿠폰 상세 조회")
    @GetMapping("/coupons/my/{userCouponId}")
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
    @Operation(summary = "쿠폰 사용 수 현황 조회")
    @GetMapping("/coupon/usage-status")
    public ResponseEntity<CouponUsageStatusResponse> getCouponStatusCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();

        CouponUsageStatusResponse response = userCouponService.getCouponUsageStats(userUuid);
        return ResponseEntity.ok(response);
    }
}
