package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_link")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    @Column(nullable = false)
    private String url;

    private Boolean isPrimary;
}
