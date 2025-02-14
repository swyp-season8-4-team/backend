package org.swyp.dessertbee.store.menu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @Column(name = "menu_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator
    private UUID menuUuid;

    @Column(name = "store_id")
    private Long storeId;
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Boolean isPopular;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public void update(String name, BigDecimal price, Boolean isPopular, String description) {
        this.name = name;
        this.price = price;
        this.isPopular = isPopular;
        this.description = description;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
