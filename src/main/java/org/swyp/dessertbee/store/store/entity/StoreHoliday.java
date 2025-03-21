package org.swyp.dessertbee.store.store.entity;

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
    private LocalDate holidayDate; // 특정 휴무일

    private String reason; // 휴무 사유

    @CreationTimestamp
    private LocalDateTime createdAt;
}

