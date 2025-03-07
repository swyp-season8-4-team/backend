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
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    /**
     * 다중 이미지 업로드
     */
    @Transactional
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

    /**
     * 단일 이미지 업로드
     */
    public void uploadAndSaveImage(MultipartFile file, ImageType refType, Long refId, String folder) {
        if (file == null) return;

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
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - type: {}, refId: {}", refType, refId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 단일 이미지 조회
     */
    public String getImageByTypeAndId(ImageType refType, Long refId) {
        if (refId == null) {
            log.error("이미지 조회 실패 - refId가 null입니다. refType: {}", refType);
            return "";
        }

        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);

        // 로그 추가
        log.info("조회된 이미지 개수: {}, refType: {}, refId: {}", images.size(), refType, refId);

        return images.stream().map(Image::getUrl).findFirst().orElse("");

    }

    /**
     * 특정 refType과 refId에 해당하는 이미지 조회 (URL 반환)
     */
    public List<String> getImagesByTypeAndId(ImageType refType, Long refId) {
        if (refId == null) {
            log.error("이미지 조회 실패 - refId가 null입니다. refType: {}", refType);
            return List.of();
        }

        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);

        // 로그 추가
        log.info("조회된 이미지 개수: {}, refType: {}, refId: {}", images.size(), refType, refId);

        return imageRepository.findByRefTypeAndRefId(refType, refId)
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * 외부 URL에서 이미지를 다운로드하여 S3에 업로드하고 DB에 저장
     */
    @Transactional
    public void downloadAndSaveImage(String imageUrl, ImageType refType, Long refId, String folder) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        try {
            // 파일명 생성 (URL에서 파일명 추출 또는 기본값 사용)
            String fileName = extractFileNameFromUrl(imageUrl);

            // 이미지 다운로드 및 MultipartFile로 변환
            MultipartFile imageFile = downloadImageFromUrl(imageUrl, fileName);

            // S3에 업로드 및 DB에 저장 (기존 메서드 활용)
            uploadAndSaveImage(imageFile, refType, refId, folder);

            log.info("외부 이미지 다운로드 및 저장 성공 - type: {}, refId: {}, url: {}",
                    refType, refId, imageUrl);
        } catch (Exception e) {
            log.error("외부 이미지 다운로드 및 저장 실패 - type: {}, refId: {}, url: {}",
                    refType, refId, imageUrl, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * URL에서 파일명을 추출하는 메서드
     */
    private String extractFileNameFromUrl(String imageUrl) {
        try {
            // URL에서 파일명 부분 추출 시도
            String[] parts = imageUrl.split("/");
            String lastPart = parts[parts.length - 1];

            // 파일명에 쿼리 파라미터가 있는 경우 제거
            if (lastPart.contains("?")) {
                lastPart = lastPart.substring(0, lastPart.indexOf("?"));
            }

            // 파일명이 비어있거나 추출할 수 없는 경우 기본값 사용
            if (lastPart.isEmpty()) {
                return "downloaded-image.jpg";
            }

            return lastPart;
        } catch (Exception e) {
            // 파일명 추출 실패 시 기본값 반환
            return "downloaded-image.jpg";
        }
    }

    /**
     * URL에서 이미지를 다운로드하여 MultipartFile로 변환하는 메서드
     */
    private MultipartFile downloadImageFromUrl(String imageUrl, String fileName) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] imageBytes = outputStream.toByteArray();

            // 이미지 타입 감지 (기본값은 image/jpeg)
            String contentType = connection.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg";
            }
            // MockMultipartFile 생성
            return new MockMultipartFile(
                    fileName,  // 필드 이름으로 파일명 사용
                    fileName,  // 원본 파일명
                    contentType,
                    imageBytes
            );
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 여러 refId에 해당하는 이미지 조회
     */
    public Map<Long, List<String>> getImagesByTypeAndIds(ImageType type, List<Long> refIds) {
        List<Image> images = imageRepository.findByRefTypeAndRefIdIn(type, refIds);
        return images.stream()
                .collect(Collectors.groupingBy(Image::getRefId,
                        Collectors.mapping(Image::getUrl, Collectors.toList())));
    }

    /**
     * 기존 이미지 삭제 후 새 이미지 업로드
     */
    @Transactional
    public void updateImage(ImageType refType, Long refId, MultipartFile newFile, String folder) {
        deleteImagesByRefId(refType, refId);
        uploadAndSaveImage(newFile, refType, refId, folder);
    }

    /**
     * 기존 이미지 일부 삭제 후 새로운 이미지 추가
     */
    @Transactional
    public void updatePartialImages(List<Long> deleteImageIds, List<MultipartFile> newFiles, ImageType refType, Long refId, String folder) {
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteImagesByIds(deleteImageIds);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            uploadAndSaveImages(newFiles, refType, refId, folder);
        }
    }

    /**
     * 특정 refType과 refId를 가진 모든 이미지 삭제
     */
    @Transactional
    public void deleteImagesByRefId(ImageType refType, Long refId) {
        List<Image> images = imageRepository.findByRefTypeAndRefId(refType, refId);
        deleteImages(images);
    }

    /**
     * 여러 개의 이미지 ID 삭제
     */
    @Transactional
    public void deleteImagesByIds(List<Long> imageIds) {
        List<Image> images = imageRepository.findAllById(imageIds);
        deleteImages(images);
    }

    /**
     * S3 및 DB에서 이미지 삭제
     */
    private void deleteImages(List<Image> images) {
        if (images.isEmpty()) return;

        images.forEach(image -> s3Service.deleteFile(image.getUrl()));

        imageRepository.deleteAll(images);
    }

    /**
     * 다중 이미지 순서 넣고 업로드
     */
    public Image uploadAndSaveImages(MultipartFile file, ImageType refType, Long refId, String folder, Integer idx) {
        if (file == null) return null;

        try {
            String url = s3Service.uploadFile(file, folder);
            Image image = Image.builder()
                    .refType(refType)
                    .refId(refId)
                    .path(folder)
                    .fileName(file.getOriginalFilename())
                    .url(url)
                    .imageIndex(idx)
                    .build();

            imageRepository.save(image);

            log.info("이미지 업로드 성공 - type: {}, refId: {}", refType, refId);
            return image;
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - type: {}, refId: {}", refType, refId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}
