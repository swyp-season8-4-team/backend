package org.swyp.dessertbee.store.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.userdetails.User;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
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

    private String name;

    @Column(unique = true)
    private String code; // 확인 코드 (QR 대상)

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private CouponStatus status; //ISSUED, USED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    public void markAsUsed() {
        if (this.status != CouponStatus.ISSUED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
