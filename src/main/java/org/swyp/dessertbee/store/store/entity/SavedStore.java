package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_store_list_id", nullable = false)
    private UserStoreList userStoreList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @CreationTimestamp
    private LocalDateTime createdAt;
}