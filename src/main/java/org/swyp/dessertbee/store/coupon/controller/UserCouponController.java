package org.swyp.dessertbee.store.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.store.coupon.service.UserCouponService;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class UserCouponController {


    private final UserCouponService userCouponService;


    @PostMapping("/issue")
    public ResponseEntity<IssuedCouponResponse> issueCoupon(
            @RequestBody IssueCouponRequest request,
            @AuthenticationPrincipal UserEntity user // Spring Security로 로그인 유저 주입
    ) {
        return ResponseEntity.ok(userCouponService.issueCoupon(request, user));
    }


    @GetMapping("/my")
    public ResponseEntity<List<IssuedCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal UserEntity user
    ) {
        return ResponseEntity.ok(userCouponService.getUserCoupons(user.getUserUuid()));
    }

}
