package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.StoreTag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StoreTagRepository extends JpaRepository<StoreTag, Long> {

    /* 태그 한 번에 조회 */
    List<StoreTag> findByNameIn(Set<String> names);
}
