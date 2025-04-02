package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate createDate; // 가게가 등록된 날짜

    @CreationTimestamp
    private LocalDateTime createdAt; // 가게 통계가 집계된 시간

    private LocalDateTime deletedAt; // 가게, 통계가 삭제(무효화)된 시간

    public void increaseViews() {
        this.views = this.views + 1;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
