package org.swyp.dessertbee.store.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.store.coupon.service.UserCouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class UserCouponController {


    private final UserCouponService userCouponService;


    @PostMapping("/issue")
    public ResponseEntity<IssuedCouponResponse> issueCoupon(@RequestBody IssueCouponRequest request) {
        return ResponseEntity.ok(userCouponService.issueCoupon(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<IssuedCouponResponse>> getUserCoupons(@PathVariable Long userId) {
        return ResponseEntity.ok(userCouponService.getUserCoupons(userId));
    }

}
