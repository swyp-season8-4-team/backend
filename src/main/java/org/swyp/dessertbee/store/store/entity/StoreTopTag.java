package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 가게를 저장한 사용자들의 취향 태그 Top3
 */
@Entity
@Table(name = "store_top_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTopTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "tag_rank", nullable = false)
    private Integer tagRank;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
