package org.swyp.dessertbee.store.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponConditionType;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponTarget;
import org.swyp.dessertbee.store.coupon.entity.enums.DiscountType;
import org.swyp.dessertbee.store.coupon.entity.enums.CouponType;
import org.swyp.dessertbee.store.store.entity.Store;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status; // CREATED, USED, EXPIRED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type; // DISCOUNT, GIFT

    // 할인 쿠폰일 경우
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // FIXED, RATE
    private Integer discountAmount; // 정액 할인 금액 또는 정률 할인율

    // 증정 쿠폰일 경우
    private String giftMenuName;

    // 쿠폰 제공 대상
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponTarget couponTarget; // ALL, SUBSCRIBED, CUSTOM

    // 노출 기한
    @Column(nullable = false)
    private Boolean hasExposureDate = true;
    private LocalDateTime exposureStartAt;
    private LocalDateTime exposureEndAt;

    // 쿠폰 사용 조건
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponConditionType conditionType; // AMOUNT, TIME_DAY, EXCLUSIVE, CUSTOM

    private Integer minimumPurchaseAmount; // AMOUNT 조건일 경우

    private LocalTime conditionStartTime; // TIME_DAY 조건일 경우
    private LocalTime conditionEndTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coupon_condition_days", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> conditionDays; // TIME_DAY 조건일 경우

    private Boolean exclusiveOnly=false; //EXCLUSIVE 조건일 경우

    private String customConditionText; // CUSTOM 조건일 경우

    // 유효 기간
    @Column(nullable = false)
    private Boolean hasExpiryDate = true;
    private LocalDateTime expiryDate;

    // 수량 제한
    @Column(nullable = false)
    private Boolean hasQuantity = true;
    private Integer quantity;

    private LocalDateTime createdAt;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCoupon> userCoupons = new ArrayList<>();

    public void markAsUsed() {
        if (this.status != CouponStatus.CREATED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
