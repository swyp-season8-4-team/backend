package org.swyp.dessertbee.user.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.swyp.dessertbee.store.coupon.entity.Coupon;
import org.swyp.dessertbee.user.entity.UserEntity;

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

    @Builder.Default
    private boolean isUsed = false;

    @Builder.Default
    private boolean isExpired=false;

    private long usedCount;
    private long unusedCount;
    private long expiredCount;

    @Lob
    private String qrImageUrl; // base64 혹은 URL 저장

    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.isUsed = true;
    }

    public void expire() {
        this.isExpired = true;
    }

    public void CouponUsageStatus(long usedCount, long unusedCount, long expiredCount) {
        this.usedCount = usedCount;
        this.unusedCount = unusedCount;
        this.expiredCount = expiredCount;
    }
}
