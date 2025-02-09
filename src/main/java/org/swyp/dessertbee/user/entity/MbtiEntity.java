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

    @Column(name = "type", length = 4, nullable = false, unique = true)
    private String type;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "desc", length = 200)
    private String desc;
}