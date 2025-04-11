package org.swyp.dessertbee.store.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.userdetails.User;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(name = "coupon_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID couponUuid;

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponStatus status; //ISSUED, USED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCoupon> userCoupons = new ArrayList<>();

    public void markAsUsed() {
        if (this.status != CouponStatus.ISSUED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
