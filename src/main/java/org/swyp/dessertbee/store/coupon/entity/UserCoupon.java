package org.swyp.dessertbee.store.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    private boolean isUsed = false;


    @Column(unique = true, nullable = false)
    private String code;


    public UserCoupon(Long userId, Coupon coupon) {
        this.userId = userId;
        this.coupon = coupon;
        this.code = UUID.randomUUID().toString(); // QR에 들어갈 고유 식별자
    }

    public void use() {
        this.isUsed = true;
    }
}
