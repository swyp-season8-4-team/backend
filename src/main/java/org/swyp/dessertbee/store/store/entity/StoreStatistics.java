package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store_statistics")
public class StoreStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statisticsId;

    @Column(name = "store_id")
    private Long storeId;

    private Integer views;
    private Integer saves;
    private Integer reviews;
    private LocalDate createDate;
    private LocalDateTime createdAt;

    public void increaseViews() {
        this.views = this.views + 1;
    }
}
