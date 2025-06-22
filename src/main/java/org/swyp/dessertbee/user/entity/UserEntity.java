package org.swyp.dessertbee.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.UserRoleEntity;
import org.swyp.dessertbee.user.coupon.entity.UserCoupon;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID userUuid;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String name;

    @Column(length = 50)
    private String nickname;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "phone_number", length = 13)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 6)
    private Gender gender;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil; // 정지 만료일

    @Column(name = "write_restricted_until")
    private LocalDateTime writeRestrictedUntil; //작성제한 만료일

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthEntity> authEntities = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPreferenceEntity> userPreferences = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCoupon> userCoupons = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbti_id", nullable = true)
    private MbtiEntity mbti;

    // 의도적으로 선호도 설정을 안하겠다는 의미로 사용
    @Builder.Default
    @Column(name = "preference_set_flag", nullable = false)
    private boolean preferenceSetFlag = false;

    public enum Gender {
        MALE,
        FEMALE
    }

    // ==== 도메인 메서드 ====

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            String numberOnly = phoneNumber.replaceAll("-", "");
            this.phoneNumber = numberOnly.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else {
            this.phoneNumber = null;
        }
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateMbti(MbtiEntity mbti) {
        this.mbti = mbti;
    }

    public void removeMbti() {
        this.mbti = null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void markPreferenceSet() {
        this.preferenceSetFlag = true;
    }

    public void unmarkPreferenceSet() {
        this.preferenceSetFlag = false;
    }

    public void addRole(RoleEntity role) {
        UserRoleEntity userRole = UserRoleEntity.builder()
                .user(this)
                .role(role)
                .build();
        this.userRoles.add(userRole);
    }

    public void removeRole(UserRoleEntity userRole) {
        this.userRoles.remove(userRole);
        userRole.clearUser();
    }

    // 정지 처리
    public void suspendForOneMonth() {
        this.suspendedUntil = LocalDateTime.now().plusMonths(1);
    }

    public void unsuspend() {
        this.suspendedUntil = null;
    }

    // 정지 여부
    public boolean isSuspended() {
        return suspendedUntil != null && LocalDateTime.now().isBefore(suspendedUntil);
    }

    // 작성 제한 처리
    public void restrictWritingFor7Days() {
        this.writeRestrictedUntil = LocalDateTime.now().plusDays(7);
    }

    //작성 제한 여부
    public boolean isWriteRestricted() {
        return writeRestrictedUntil != null && LocalDateTime.now().isBefore(writeRestrictedUntil);
    }

}