package org.swyp.dessertbee.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false)
    private ImageType refType; // 'STORE', 'OWNERPICK', 'MENU', 'REVIEW', 'PROFILE', 'MATE', 'SHORT'

    @Column(name = "ref_id", nullable = false)
    private Long refId; // 참조 ID

    @Column(nullable = false)
    private String path; // S3 경로 추가

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    @Column
    private UUID imageUuid;
}
