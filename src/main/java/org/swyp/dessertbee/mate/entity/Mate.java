package org.swyp.dessertbee.mate.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(name = "mate_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator()
    private UUID mateUuid;


    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "mate_category_id")
    private Long mateCategoryId; //메이트 카테고리(ex:친목,빵지순례 등등)

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "place_name")
    private String placeName;

    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;


    @Column(name = "recruit_yn")
    private Boolean recruitYn;  //메이트 현재 모집 여부

    @Column(nullable = true, length = 255)
    private String report;


    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public void update(String title, String content, Boolean recruitYn, Long mateCategoryId){
        this.title = title;
        this.content = content;
        this.recruitYn = recruitYn;
        this.mateCategoryId = mateCategoryId;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
