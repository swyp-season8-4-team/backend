package org.swyp.dessertbee.store.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.tag.entity.StoreTag;

@Entity
@Table(name = "store_tag_relation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreTagRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private StoreTag tag;
}
