package org.swyp.dessertbee.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mbti")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbtiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mbti_type", length = 4, nullable = false, unique = true)
    private String mbtiType;

    @Column(name = "mbti_name", length = 20, nullable = false)
    private String mbtiName;

    @Column(name = "mbti_desc", length = 200)
    private String mbtiDesc;
}