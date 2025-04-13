package org.swyp.dessertbee.store.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.User;
import org.swyp.dessertbee.user.entity.UserEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    @Column(unique = true, nullable = false)
    private String couponCode; // QR에 들어갈 고유 식별자

    private boolean isUsed = false;

    @Lob
    private String qrImageUrl; // base64 혹은 URL 저장

    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.isUsed = true;
    }
}
