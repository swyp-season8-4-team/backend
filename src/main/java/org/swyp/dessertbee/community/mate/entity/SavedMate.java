package org.swyp.dessertbee.community.mate.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_mate")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedMate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_mate_id")
    private Long savedMateId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id", referencedColumnName = "mate_id", nullable = false) // ✅ referencedColumnName 추가
    private Mate mate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}

