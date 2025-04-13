package org.swyp.dessertbee.store.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.store.coupon.service.CouponService;
import org.swyp.dessertbee.user.coupon.service.UserCouponService;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;

@RestController
@Tag(name = "Coupon", description = "가게 쿠폰 관련 API")
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final StoreRepository storeRepository;

    /**
     * 쿠폰 생성 (1인 1쿠폰)
     */
    @Operation(summary = "쿠폰 생성")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest request) {
        Store store = storeRepository.findByStoreUuid(request.getStoreUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return ResponseEntity.ok(couponService.createCoupon(request, store));
    }
    /**
     * 쿠폰 수정
     */
    @Operation(summary = "쿠폰 수정")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody @Valid CouponRequest request
    ) {
        Store store = storeRepository.findByStoreUuid(request.getStoreUuid())
                .orElseThrow(() ->new BusinessException(ErrorCode.STORE_NOT_FOUND));
        CouponResponse updated = couponService.updateCoupon(couponId, request, store);
        return ResponseEntity.ok(updated);
    }

    /**
     * 쿠폰 삭제
     */
    @Operation(summary = "쿠폰 삭제")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long couponId
            ) {
        couponService.deleteCoupon(couponId);

        return ResponseEntity.noContent().build(); // 삭제 성공 시, 204 No Content 응답
    }
    /**
     * 생성한 쿠폰 조회
     */
    @Operation(summary = "생성한 쿠폰 조회")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    /**
     * 쿠폰 사용처리
     */
    @Operation(summary = "쿠폰 사용 처리")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/use")
    public ResponseEntity<UsedCouponResponse> useCoupon(@RequestBody UseCouponRequest request) {
        UsedCouponResponse response = userCouponService.useCouponByCode(request);
        return ResponseEntity.ok(response);
    }

}