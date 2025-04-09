package org.swyp.dessertbee.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "login_attempt")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LoginAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 계정 잠금 상태 여부 확인
     */
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 로그인 실패 횟수 증가 및 계정 잠금 설정
     */
    public void incrementFailedAttempts(int maxFailedAttempts, int lockTimeMinutes) {
        this.failedAttempts++;
        // 최대 실패 횟수 이상 시 계정 잠금
        if (this.failedAttempts >= maxFailedAttempts) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockTimeMinutes);
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * 계정 잠금 해제까지 남은 시간(분) 계산
     */
    public long getRemainingLockTime() {
        if (!isAccountLocked()) {
            return 0;
        }
        return Duration.between(LocalDateTime.now(), lockedUntil).toMinutes() + 1;
    }
}