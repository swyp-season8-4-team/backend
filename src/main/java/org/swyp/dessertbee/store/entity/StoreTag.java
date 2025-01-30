package org.swyp.dessertbee.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 태그 고유번호

    @Column(nullable = false, unique = true, length = 100)
    private String name; // 태그명
}
