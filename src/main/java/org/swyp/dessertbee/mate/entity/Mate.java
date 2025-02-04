package org.swyp.dessertbee.mate.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="mate")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mate {

    @Id
    @Column(name = "mate_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mateId;    //메이트 고유 id

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "mate_category_id")
    private Long mateCategoryId; //메이트 카테고리(ex:친목,빵지순례 등등)

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "recruit_yn")
    private Boolean recruitYn;  //메이트 현재 모집 여부

    @Column(nullable = true, length = 255)
    private String report;


    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;



}
