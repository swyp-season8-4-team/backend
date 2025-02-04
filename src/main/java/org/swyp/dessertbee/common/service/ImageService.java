package org.swyp.dessertbee.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${s3.base-url}")
    private String s3BaseUrl;

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    /** S3에 업로드 후 DB에 저장 */
    public void uploadAndSaveImages(List<MultipartFile> files, ImageType refType, Long refId, String folder) {
        if (files == null || files.isEmpty()) return;

        List<Image> images = files.stream()
                .map(file -> {
                    String url = s3Service.uploadFile(file, folder); // ✅ folder 인자 추가
                    return Image.builder()
                            .refType(refType)
                            .refId(refId)
                            .path(folder) // ✅ 저장되는 경로도 정확하게 설정
                            .fileName(file.getOriginalFilename())
                            .url(url)
                            .build();
                })
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

    /** 특정 refType과 refId를 가진 모든 이미지 삭제 (S3 + DB) */
    public void deleteImagesByRefId(ImageType refType, Long refId) {
        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);

        if (images.isEmpty()) {
            return; // 삭제할 이미지가 없으면 종료
        }

        // S3에서 이미지 삭제
        for (Image image : images) {
            s3Service.deleteFile(image.getPath(), image.getFileName());
        }

        // DB에서 이미지 삭제
        imageRepository.deleteAll(images);
    }

    /** 특정 이미지 1개 삭제 */
    public void deleteImageById(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다."));

        // S3에서 삭제
        s3Service.deleteFile(image.getPath(), image.getFileName());

        // DB에서 삭제
        imageRepository.delete(image);
    }

    /** 기존 이미지 삭제 후 새 이미지 업로드 (update) */
    public void updateImages(List<MultipartFile> files, ImageType refType, Long refId, String folder) {
        // 기존 이미지 삭제
        deleteImagesByRefId(refType, refId);

        // 새 이미지 업로드
        uploadAndSaveImages(files, refType, refId, folder);
    }
}
