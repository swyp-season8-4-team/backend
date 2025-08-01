package org.swyp.dessertbee.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private static final String BASE_URL = "https://desserbee-bucket-new.s3.ap-northeast-2.amazonaws.com/";

    /** S3에 파일 업로드 후 URL 반환 */
    public String uploadFile(MultipartFile file, String folder) {
        if (!folder.endsWith("/")) {
            folder += "/";
        }

        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return BASE_URL + fileName;

        } catch (IOException e) {
            log.error("S3 업로드 실패 - 파일명: {}, 에러: {}", fileName, e.getMessage());
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /** S3에서 파일 삭제 */
    public void deleteFile(String url) {
        if (!url.startsWith(BASE_URL)) {
            throw new IllegalArgumentException("유효하지 않은 S3 URL입니다.");
        }

        String key = url.substring(BASE_URL.length());

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
