package org.swyp.dessertbee.store.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.coupon.dto.request.CouponRequest;
import org.swyp.dessertbee.user.coupon.dto.request.UseCouponRequest;
import org.swyp.dessertbee.store.coupon.dto.response.CouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UsedCouponResponse;
import org.swyp.dessertbee.store.coupon.service.CouponServiceImpl;
import org.swyp.dessertbee.user.coupon.service.UserCouponServiceImpl;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Coupon", description = "가게 쿠폰 관련 API")
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponServiceImpl couponServiceImpl;
    private final UserCouponServiceImpl userCouponServiceImpl;
    private final StoreRepository storeRepository;

    /**
     * 쿠폰 생성 (1인 1쿠폰)
     */
    @Operation(summary = "쿠폰 생성 (completed)", description = "가게에 새로운 쿠폰을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 생성 성공", content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    @ApiErrorResponses({
            ErrorCode.STORE_NOT_FOUND,
            ErrorCode.INVALID_COUPON_NAME,
            ErrorCode.EXPIRY_DATE_NOT_ALLOWED,
            ErrorCode.EXPOSURE_DATE_NOT_ALLOWED,
            ErrorCode.INVALID_DISCOUNT_FOR_GIFT,
            ErrorCode.INVALID_GIFT_DETAIL,
            ErrorCode.INVALID_CONDITION_TYPE,
            ErrorCode.MIN_PURCHASE_AMOUNT_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_AMOUNT,
            ErrorCode.TIME_DAY_FIELDS_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_TIME_DAY,
            ErrorCode.CUSTOM_CONDITION_TEXT_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_CUSTOM,
            ErrorCode.EXCLUSIVE_FIELD_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_EXCLUSIVE
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest request) {
        Store store = storeRepository.findByStoreUuid(request.getStoreUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return ResponseEntity.ok(couponServiceImpl.createCoupon(request, store));
    }
    /**
     * 쿠폰 수정
     */
    @Operation(summary = "쿠폰 수정 (completed)", description = "기존 쿠폰 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 생성 성공", content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    @ApiErrorResponses({
            ErrorCode.STORE_NOT_FOUND,
            ErrorCode.COUPON_NOT_FOUND,
            ErrorCode.INVALID_COUPON_NAME,
            ErrorCode.EXPIRY_DATE_NOT_ALLOWED,
            ErrorCode.EXPOSURE_DATE_NOT_ALLOWED,
            ErrorCode.INVALID_DISCOUNT_FOR_GIFT,
            ErrorCode.INVALID_GIFT_DETAIL,
            ErrorCode.INVALID_CONDITION_TYPE,
            ErrorCode.MIN_PURCHASE_AMOUNT_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_AMOUNT,
            ErrorCode.TIME_DAY_FIELDS_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_TIME_DAY,
            ErrorCode.CUSTOM_CONDITION_TEXT_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_CUSTOM,
            ErrorCode.EXCLUSIVE_FIELD_REQUIRED,
            ErrorCode.EXTRA_FIELDS_NOT_ALLOWED_FOR_EXCLUSIVE
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody @Valid CouponRequest request
    ) {
        Store store = storeRepository.findByStoreUuid(request.getStoreUuid())
                .orElseThrow(() ->new BusinessException(ErrorCode.STORE_NOT_FOUND));
        CouponResponse updated = couponServiceImpl.updateCoupon(couponId, request, store);
        return ResponseEntity.ok(updated);
    }

    /**
     * 쿠폰 삭제
     */
    @Operation(summary = "쿠폰 삭제 (completed)", description = "가게에서 발급한 쿠폰을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "쿠폰 삭제 성공")
    @ApiErrorResponses({
            ErrorCode.COUPON_NOT_FOUND
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long couponId
            ) {
        couponServiceImpl.deleteCoupon(couponId);

        return ResponseEntity.noContent().build(); // 삭제 성공 시, 204 No Content 응답
    }
    /**
     * 생성한 쿠폰 조회
     */
    @Operation(summary = "생성한 쿠폰 조회 (completed)", description = "가게가 생성한 모든 쿠폰을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CouponResponse.class))))
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/{storeUuid}")
    public ResponseEntity<List<CouponResponse>> getCouponsByStore(
            @PathVariable UUID storeUuid
    ) {
        Store store = storeRepository.findByStoreUuid(storeUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        List<CouponResponse> coupons = couponServiceImpl.getCouponsByStore(store);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 쿠폰 사용처리
     */
    @Operation(summary = "쿠폰 사용 처리 (completed)", description = "발급된 쿠폰을 사용 처리합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 사용 처리 성공", content = @Content(schema = @Schema(implementation = UsedCouponResponse.class)))
    @ApiErrorResponses({
            ErrorCode.USER_COUPON_NOT_FOUND,
            ErrorCode.ALREADY_USED_COUPON,
            ErrorCode.COUPON_OUT_OF_STOCK
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/use")
    public ResponseEntity<UsedCouponResponse> useCoupon(@RequestBody UseCouponRequest request) {
        UsedCouponResponse response = userCouponServiceImpl.useCouponByCode(request);
        return ResponseEntity.ok(response);
    }

}