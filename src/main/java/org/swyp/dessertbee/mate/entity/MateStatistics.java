package org.swyp.dessertbee.mate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "mate_statistics")
public class MateStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mate_statistics_id")
    private Long mateStatisticsId;

    @Column(name = "mate_id")
    private Long mateId;
    private Integer views;
    private Integer saves;
    private Integer reviews;
    private LocalDate createDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}