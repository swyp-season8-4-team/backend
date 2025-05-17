package org.swyp.dessertbee.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_block",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_block", columnNames = {"blocker_user_id", "blocked_user_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 차단한 사용자 ID (FK)
     */
    @Column(name = "blocker_user_id", nullable = false)
    private Long blockerUserId;

    /**
     * 차단된 사용자 ID (FK)
     */
    @Column(name = "blocked_user_id", nullable = false)
    private Long blockedUserId;

    /**
     * 차단한 사용자 (관계 매핑)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_user_id", insertable = false, updatable = false)
    private UserEntity blockerUser;

    /**
     * 차단된 사용자 (관계 매핑)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", insertable = false, updatable = false)
    private UserEntity blockedUser;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}