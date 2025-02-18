package org.swyp.dessertbee.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    /** 다중 이미지 업로드 후 URL 반환 */
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

    /** 단일 이미지 업로드 후 URL 반환 */
    public String uploadAndSaveImage(MultipartFile file, ImageType refType, Long refId, String folder) {
        if (file == null) return null;

        try {
            String url = s3Service.uploadFile(file, folder);
            Image image = Image.builder()
                    .refType(refType)
                    .refId(refId)
                    .path(folder)
                    .fileName(file.getOriginalFilename())
                    .url(url)
                    .build();

            imageRepository.save(image);
            log.info("이미지 업로드 성공 - type: {}, refId: {}", refType, refId);
            return url;
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - type: {}, refId: {}", refType, refId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /** 특정 refType과 refId에 해당하는 이미지 조회 (URL 반환) */
    public List<String> getImagesByTypeAndId(ImageType refType, Long refId) {
        return imageRepository.findByRefTypeAndRefId(refType, refId)
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
    }

    /** 여러 refId에 해당하는 이미지 조회 */
    public Map<Long, List<String>> getImagesByTypeAndIds(ImageType type, List<Long> refIds) {
        List<Image> images = imageRepository.findByRefTypeAndRefIdIn(type, refIds);
        return images.stream()
                .collect(Collectors.groupingBy(Image::getRefId,
                        Collectors.mapping(Image::getUrl, Collectors.toList())));
    }

    /** 기존 이미지 삭제 후 새 이미지 업로드 */
    @Transactional
    public void updateImage(ImageType refType, Long refId, MultipartFile newFile, String folder) {
        deleteImagesByRefId(refType, refId);
        uploadAndSaveImage(newFile, refType, refId, folder);
    }

    /** 기존 이미지 일부 삭제 후 새로운 이미지 추가 */
    @Transactional
    public void updatePartialImages(List<Long> deleteImageIds, List<MultipartFile> newFiles, ImageType refType, Long refId, String folder) {
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteImagesByIds(deleteImageIds);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            uploadAndSaveImages(newFiles, refType, refId, folder);
        }
    }

    /** 특정 refType과 refId를 가진 모든 이미지 삭제 */
    @Transactional
    public void deleteImagesByRefId(ImageType refType, Long refId) {
        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);
        deleteImages(images);
    }

    /** 여러 개의 이미지 ID 삭제 */
    @Transactional
    public void deleteImagesByIds(List<Long> imageIds) {
        List<Image> images = imageRepository.findAllById(imageIds);
        deleteImages(images);
    }

    /** S3 및 DB에서 이미지 삭제 */
    private void deleteImages(List<Image> images) {
        if (images.isEmpty()) return;

        images.forEach(image -> s3Service.deleteFile(image.getUrl()));

        imageRepository.deleteAll(images);
    }
}
