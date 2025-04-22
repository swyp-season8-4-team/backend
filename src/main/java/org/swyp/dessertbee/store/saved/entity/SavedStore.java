package org.swyp.dessertbee.store.saved.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.swyp.dessertbee.store.saved.entity.UserStoreList;
import org.swyp.dessertbee.store.store.entity.Store;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "saved_store")
@Getter
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

    @ElementCollection
    @CollectionTable(name = "saved_store_preferences", joinColumns = @JoinColumn(name = "saved_store_id"))
    @Column(name = "preference", nullable = false)
    private List<Long> userPreferences;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void updateUserPreferences(List<Long> newPreferences) {
        this.userPreferences = newPreferences;
    }
}