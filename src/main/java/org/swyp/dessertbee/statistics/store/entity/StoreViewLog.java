package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_view_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreViewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private UUID userUuid;

    private LocalDateTime viewedAt;
}