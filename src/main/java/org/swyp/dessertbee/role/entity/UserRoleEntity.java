package org.swyp.dessertbee.role.entity;

import jakarta.persistence.*;
import lombok.*;
import org.swyp.dessertbee.user.entity.UserEntity;

@Entity
@Table(name = "user_role")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    public void clearUser() {
        this.user = null;
    }
}
