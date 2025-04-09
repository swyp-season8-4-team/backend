package org.swyp.dessertbee.store.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.store.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.store.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.store.coupon.repository.CouponRepository;
import org.swyp.dessertbee.store.coupon.service.CouponService;
import org.swyp.dessertbee.store.coupon.service.UserCouponService;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    //사장님만 가능하게

    private final CouponService couponService;
    private final UserCouponService userCouponService;

    /**
     * 쿠폰 생성 (1인 1쿠폰)
     */
    @PostMapping("/create")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    /**
     * 생성한 쿠폰 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/use")
    public ResponseEntity<UsedCouponResponse> useCoupon(@RequestBody UseCouponRequest request) {
        UsedCouponResponse response = userCouponService.useCouponByCode(request);
        return ResponseEntity.ok(response);
    }
}