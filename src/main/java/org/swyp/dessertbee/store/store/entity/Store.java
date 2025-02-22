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
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String storeLink;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;

    private String description;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StoreStatus status = StoreStatus.APPROVED;

    private LocalDateTime deletedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
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

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
