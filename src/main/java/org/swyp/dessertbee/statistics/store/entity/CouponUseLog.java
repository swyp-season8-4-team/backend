package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon_use_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUseLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private UUID userUuid;

    private LocalDateTime usedAt;

    private String couponCode; // todo: 쿠폰 식별자 역할 (미정)
}