package org.swyp.dessertbee.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    /** S3에 업로드 후 DB에 저장 */
    public void uploadAndSaveImages(List<MultipartFile> files, ImageType refType, Long refId, String folder) {
        if (files == null || files.isEmpty()) return;

        List<Image> images = files.stream()
                .map(file -> {
                    String url = s3Service.uploadFile(file, folder);
                    return Image.builder()
                            .refType(refType)
                            .refId(refId)
                            .path(folder)
                            .fileName(file.getOriginalFilename())
                            .url(url)
                            .build();
                })
                .collect(Collectors.toList());

        imageRepository.saveAll(images);
    }

    /** 단일 이미지 업로드 */
    public void uploadAndSaveImage(MultipartFile file, ImageType refType, Long refId, String folder) {
        if (file == null) return;

        String url = s3Service.uploadFile(file, folder);
        Image image = Image.builder()
                .refType(refType)
                .refId(refId)
                .path(folder)
                .fileName(file.getOriginalFilename())
                .url(url)
                .build();

        imageRepository.save(image);
    }

    /** 특정 refType과 refId에 해당하는 이미지 조회 */
    public List<String> getImagesByTypeAndId(ImageType refType, Long refId) {
        return imageRepository.findByRefTypeAndRefId(refType, refId)
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * 여러 refId에 해당하는 이미지 한번에 조회
     */
    public Map<Long, List<String>> getImagesByTypeAndIds(ImageType type, List<Long> refIds) {
        List<Image> images = imageRepository.findByRefTypeAndRefIdIn(type, refIds);

        return images.stream()
                .collect(Collectors.groupingBy(
                        Image::getRefId,
                        Collectors.mapping(Image::getUrl, Collectors.toList())
                ));
    }

    /** 특정 refType과 refId를 가진 모든 이미지 삭제 (S3 + DB) */
    @Transactional
    public void deleteImagesByRefId(ImageType refType, Long refId) {
        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);
        deleteImages(images);
    }

    /** 여러 개의 이미지 ID를 받아 한 번에 삭제 */
    @Transactional
    public void deleteImagesByIds(List<Long> imageIds) {
        List<Image> images = imageRepository.findAllById(imageIds);
        deleteImages(images);
    }

    /** S3 및 DB에서 이미지 삭제 */
    private void deleteImages(List<Image> images) {
        if (images.isEmpty()) return;

        images.forEach(image -> s3Service.deleteFile(image.getPath(), image.getFileName()));
        imageRepository.deleteAll(images);
    }

    /** 기존 이미지 삭제 후 새 이미지 업로드 */
    @Transactional
    public void updateImage(ImageType refType, Long refId, MultipartFile newFile, String folder) {
        deleteImagesByRefId(refType, refId);
        uploadAndSaveImage(newFile, refType, refId, folder);
    }

    /** 특정 이미지 ID들만 삭제하고 새 이미지 추가 */
    @Transactional
    public void updatePartialImages(List<Long> deleteImageIds, List<MultipartFile> newFiles, ImageType refType, Long refId, String folder) {
        // 기존 이미지 중 선택한 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteImagesByIds(deleteImageIds);
        }

        // 새로운 이미지 업로드
        if (newFiles != null && !newFiles.isEmpty()) {
            uploadAndSaveImages(newFiles, refType, refId, folder);
        }
    }
}
