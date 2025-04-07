package org.swyp.dessertbee.store.store.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "store_break_time")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreBreakTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operating_hour_id", nullable = false)
    private Long operatingHourId;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime; // 휴게시간 시작

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime; // 휴게시간 종료

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}