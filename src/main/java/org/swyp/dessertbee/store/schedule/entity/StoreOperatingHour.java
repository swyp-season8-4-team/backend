package org.swyp.dessertbee.store.schedule.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "store_operating_hour")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOperatingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek; // 요일 (월~일)

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime; // 개점 시간

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime; // 폐점 시간

    @JsonFormat(pattern = "HH:mm")
    private LocalTime lastOrderTime; // 라스트오더 시간

    private Boolean isClosed; // 해당 요일 휴무 여부

    // 휴무 주기 설정
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RegularClosureType regularClosureType; // 정기 휴무 유형 (매주, 매월)

    @Column(length = 50)
    private String regularClosureWeeks; // 매월 휴무 주차 정보 (예: "1,3" - 첫째주, 셋째주)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

