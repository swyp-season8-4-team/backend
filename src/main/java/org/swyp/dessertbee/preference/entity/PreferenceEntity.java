package org.swyp.dessertbee.preference.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preference")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "preference_name", length = 50, nullable = false)
    private String preferenceName;

    @Column(name = "preference_desc", length = 200)
    private String preferenceDesc;

    @Builder.Default
    @OneToMany(mappedBy = "preference", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPreferenceEntity> userPreferences = new HashSet<>();
}

