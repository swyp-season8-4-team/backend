package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.statistics.store.entity.enums.DessertMateAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dessertmate_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DessertMateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private Long mateId;

    private UUID userUuid;

    @Enumerated(EnumType.STRING)
    private DessertMateAction action;

    private LocalDateTime actionAt;
}