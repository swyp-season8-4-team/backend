package org.swyp.dessertbee.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    /** 이미지 URL을 저장하는 메서드 */
    public void uploadAndSaveImages(List<String> imageUrls, ImageType refType, Long refId) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        List<Image> images = imageUrls.stream()
                .map(url -> Image.builder()
                        .refType(refType)
                        .refId(refId)
                        .url(url)
                        .build())
                .collect(Collectors.toList());

        imageRepository.saveAll(images);
    }

    /** 여러 개의 이미지 한 번에 저장 */
    public void saveAllImages(List<Image> images) {
        if (images == null || images.isEmpty()) return;
        imageRepository.saveAll(images);
    }

    /** 특정 refType과 refId에 해당하는 이미지 조회 */
    public List<String> getImagesByTypeAndId(ImageType refType, Long refId) {
        return imageRepository.findByRefTypeAndRefId(refType, refId)
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
    }

    /** 여러 refId에 해당하는 이미지 한번에 조회 */
    public Map<Long, List<String>> getImagesByTypeAndIds(ImageType type, List<Long> refIds) {
        List<Image> images = imageRepository.findByRefTypeAndRefIdIn(type, refIds);

        return images.stream()
                .collect(Collectors.groupingBy(
                        Image::getRefId,
                        Collectors.mapping(Image::getUrl, Collectors.toList())
                ));
    }

}
