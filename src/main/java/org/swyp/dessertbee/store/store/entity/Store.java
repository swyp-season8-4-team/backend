package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.swyp.dessertbee.common.util.StringListConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID storeUuid;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "owner_uuid")
    private UUID ownerUuid;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    private String address;

    @Column(name = "store_link")
    private String storeLink;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "animal_yn")
    private Boolean animalYn;

    @Column(name = "tumbler_yn")
    private Boolean tumblerYn;

    @Column(name = "parking_yn")
    private Boolean parkingYn;

    private String description;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StoreStatus status = StoreStatus.APPROVED;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> notice;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = StoreStatus.APPROVED;
        }
    }

    public void updateInfo(String name, String phone, String address,
                           BigDecimal latitude, BigDecimal longitude, String description,
                           Boolean animalYn, Boolean tumblerYn, Boolean parkingYn,
                           List<String> notice) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.animalYn = animalYn;
        this.tumblerYn = tumblerYn;
        this.parkingYn = parkingYn;
        this.notice = notice;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
