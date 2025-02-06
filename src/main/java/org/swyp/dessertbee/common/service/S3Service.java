package org.swyp.dessertbee.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadFile(MultipartFile file, String folder) {
        // "/"가 중복되지 않도록 보정
        if (!folder.endsWith("/")) {
            folder += "/";
        }

        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            System.out.println("📤 [S3 업로드 시작] " + file.getOriginalFilename() + " → " + fileName);

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            System.out.println("✅ [S3 업로드 성공] " + fileName);

            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;

        } catch (IOException e) {
            System.err.println("❌ [S3 업로드 실패] " + file.getOriginalFilename());
            e.printStackTrace();
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /** ✅ S3에서 파일 삭제 */
    public void deleteFile(String folder, String url) {

        //사진 경로가 현재 uuid 를 포함하고 있어서 db에서 받은 url에서 aws 기본 url 삭제
        String baseUrl = "https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/";
        int baseUrlLength = baseUrl.length();

        String key = url.substring(baseUrlLength);

        try {

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            System.out.println("🗑 [S3 삭제 성공] " + key);
        } catch (Exception e) {
            System.err.println("❌ [S3 삭제 실패] " + key);
            e.printStackTrace();
        }
    }
}
