package org.swyp.dessertbee.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType refType; // 'STORE', 'MENU', 'EVENT', 'REVIEW', 'PROFILE', 'MATE', 'SHORT'

    @Column(nullable = false)
    private Long refId; // 참조 ID

    @Column(nullable = false, length = 500)
    private String url;
}
