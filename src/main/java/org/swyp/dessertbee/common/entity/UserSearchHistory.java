package org.swyp.dessertbee.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_search_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String keyword;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 엔티티 객체를 생성
    public static UserSearchHistory create(Long userId, String keyword) {
        return UserSearchHistory.builder()
                .userId(userId)
                .keyword(keyword)
                .build();
    }
}