package org.swyp.dessertbee.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 인증 아이디

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity user;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider; // 프로바이더

    @Column(name = "provider_id", length = 255)
    private String providerId; // 프로바이더 아이디

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken; // 리프레쉬 토큰

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일
}
