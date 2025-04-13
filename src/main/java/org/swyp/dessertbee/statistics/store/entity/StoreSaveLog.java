package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.statistics.store.entity.enums.SaveAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_save_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSaveLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private UUID userUuid;

    @Enumerated(EnumType.STRING)
    private SaveAction action; // SAVE / UNSAVE

    private LocalDateTime actionAt;
}