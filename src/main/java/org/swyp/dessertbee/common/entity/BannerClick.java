package org.swyp.dessertbee.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banner_click")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "click_count", nullable = false)
    private int clickCount;

    public void incrementClickCount() {
        this.clickCount++;
    }
}
