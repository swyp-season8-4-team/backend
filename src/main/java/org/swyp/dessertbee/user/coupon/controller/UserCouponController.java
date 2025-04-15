package org.swyp.dessertbee.user.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.security.CustomUserDetails;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.coupon.dto.request.IssueCouponRequest;
import org.swyp.dessertbee.user.coupon.dto.response.CouponUsageStatusResponse;
import org.swyp.dessertbee.user.coupon.dto.response.IssuedCouponResponse;
import org.swyp.dessertbee.user.coupon.dto.response.UserCouponDetailResponse;
import org.swyp.dessertbee.user.coupon.service.UserCouponServiceImpl;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "UserCoupon", description = "사용자 쿠폰 관련 API")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCouponController {


    private final UserCouponServiceImpl userCouponServiceImpl;
    private final UserRepository userRepository;


    /**
     * 쿠폰 발급
     */
    @Operation(summary = "쿠폰 발급 (completed)", description = "쿠폰 UUID를 기반으로 로그인한 사용자에게 쿠폰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공"),
            @ApiResponse(responseCode = "404", description = "쿠폰 또는 사용자 정보가 존재하지 않을 경우"),
            @ApiResponse(responseCode = "400", description = "이미 발급받은 쿠폰일 경우")
    })
    @PostMapping("/coupons/issue")
    public ResponseEntity<IssuedCouponResponse> issueCoupon(
            @RequestBody IssueCouponRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(userCouponServiceImpl.issueCoupon(request, user));
    }


    /**
     * 발급 받은 쿠폰 목록 조회
     */
    @Operation(summary = "발급받은 쿠폰 목록 조회 (completed)", description = "로그인한 사용자가 발급받은 모든 쿠폰을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 시")
    })
    @GetMapping("/coupons/my")
    public ResponseEntity<List<IssuedCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        return ResponseEntity.ok(userCouponServiceImpl.getUserCoupons(userUuid));
    }

    /**
     * 발급 받은 쿠폰 상세 조회
     */
    @Operation(summary = "발급 받은 쿠폰 상세 조회", description = "특정 쿠폰 ID를 기반으로 로그인한 사용자의 쿠폰 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 쿠폰을 찾을 수 없거나 접근 권한이 없을 경우")
    })
    @GetMapping("/coupons/my/{userCouponId}")
    public ResponseEntity<UserCouponDetailResponse> getUserCouponDetail(
            @PathVariable Long userCouponId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();
        UserCouponDetailResponse response = userCouponServiceImpl.getUserCouponDetail(userCouponId, userUuid);
        return ResponseEntity.ok(response);
    }
    /**
     * 쿠폰 사용 현황 조회
     */
    @Operation(summary = "쿠폰 사용 수 현황 조회", description = "로그인한 사용자의 쿠폰 사용 현황(총 개수, 사용/미사용 수 등)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 사용 현황 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 시")
    })
    @GetMapping("/coupon/usage-status")
    public ResponseEntity<CouponUsageStatusResponse> getCouponStatusCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userUuid = userDetails.getUserUuid();

        CouponUsageStatusResponse response = userCouponServiceImpl.getCouponUsageStats(userUuid);
        return ResponseEntity.ok(response);
    }
}
