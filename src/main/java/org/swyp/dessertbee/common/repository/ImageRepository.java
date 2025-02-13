package org.swyp.dessertbee.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // 특정 ID의 이미지 조회
    List<Image> findByRefTypeAndRefId(ImageType refType, Long refId);

    // 여러 refId의 이미지 한 번에 조회
    List<Image> findByRefTypeAndRefIdIn(ImageType refType, List<Long> refIds);
}
