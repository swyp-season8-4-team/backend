package org.swyp.dessertbee.store.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank
    private String title; // 쿠폰 제목

    @NotBlank
    private String description; // 쿠폰 설명

    @NotNull
    private LocalDate expiryDate; // 만료일
}
