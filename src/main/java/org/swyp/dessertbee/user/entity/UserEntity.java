package org.swyp.dessertbee.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.UserRoleEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor // Lombok 어노테이션: 기본 생성자를 자동으로 생성
@AllArgsConstructor // Lombok 어노테이션: 모든 필드를 포함한 생성자를 자동으로 생성
@Builder // Lombok 어노테이션: 빌더 패턴으로 객체를 생성할 수 있게 함
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 활성화하여 생성일/수정일 필드를 자동 관리
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "user_uuid", nullable = false, unique = true, updatable = false)
    @UuidGenerator // Hibernate 어노테이션: UUID를 자동으로 생성하여 필드에 할당
    private UUID userUuid;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String name; // 실제이름

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(columnDefinition = "JSON")
    private String preferences; // 선호 데이터(JSON)

    @CreatedDate // JPA Auditing 어노테이션: 엔티티가 처음 저장될 때 현재 시간을 자동으로 설정
    @Column(name = "created_at", nullable = false, updatable = false) // 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate // JPA Auditing 어노테이션: 엔티티가 수정될 때 현재 시간을 자동으로 설정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at") // "deleted_at" 컬럼에 매핑됩니다.
    private LocalDateTime deletedAt;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    // "address" 컬럼에 매핑되며, 최대 길이 255자로 제한합니다.
    private String address; // 주소

    @Enumerated(EnumType.STRING) // gender 컬럼에 매핑되며, Enum 값을 문자열(String)로 저장
    @Column(name = "gender", length = 6)
    private Gender gender; // 성별

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    public void addRole(RoleEntity role) {
        UserRoleEntity userRole = UserRoleEntity.builder()
                .user(this)
                .role(role)
                .build();
        this.userRoles.add(userRole);
    }

    public void removeRole(UserRoleEntity userRole) {
        this.userRoles.remove(userRole);
        userRole.setUser(null);
    }

    public enum Gender {
        MALE,
        FEMALE
    }
}
