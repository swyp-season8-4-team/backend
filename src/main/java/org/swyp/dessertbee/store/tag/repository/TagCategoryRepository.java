package org.swyp.dessertbee.store.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.tag.entity.TagCategory;

import java.util.Optional;

@Repository
public interface TagCategoryRepository extends JpaRepository<TagCategory, Long> {
    Optional<TagCategory> findByName(String name);
}
