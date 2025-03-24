// AuthEntity.java
package org.swyp.dessertbee.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auth",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity user;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void deactivate() {
        this.active = false;
    }

    public void updateRefreshToken(String newRefreshToken, LocalDateTime expiresAt) {
        this.refreshToken = newRefreshToken;
        this.refreshTokenExpiresAt = expiresAt;
        this.active = true;
    }

    public void updateProviderId(String providerId) {
        this.providerId = providerId;
    }
}
