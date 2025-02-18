package org.swyp.dessertbee.store.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_store_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStoreList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "list_name", nullable = false, length = 50)
    private String listName;

    @Column(name= "icon_color_id", nullable = false)
    private Long iconColorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateList(String newName, Long newIconColorId) {
        this.listName = newName;
        this.iconColorId = newIconColorId;
    }
}
