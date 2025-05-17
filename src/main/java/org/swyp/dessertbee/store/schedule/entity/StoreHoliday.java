package org.swyp.dessertbee.store.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_holiday")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private LocalDate startDate; // 휴무기간 시작 일자

    @Column(nullable = false)
    private LocalDate endDate; // 휴무기간 종료 일자

    private String reason; // 휴무 사유

    @CreationTimestamp
    private LocalDateTime createdAt;
}

