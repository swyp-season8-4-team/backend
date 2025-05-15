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
                @UniqueConstraint(name = "uk_user_block", columnNames = {"blocker_id", "blocked_id"})
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
     * 차단한 사용자 ID
     */
    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    /**
     * 차단된 사용자 ID
     */
    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
