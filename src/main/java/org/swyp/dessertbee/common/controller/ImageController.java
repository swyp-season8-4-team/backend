package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /** 이미지 업로드 API */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("refType") ImageType refType,
            @RequestParam("refId") Long refId) {

        // S3 경로를 명확하게 지정
        String folder = refType.name().toLowerCase() + "/" + refId;

        imageService.uploadAndSaveImages(files, refType, refId, folder);
        return ResponseEntity.ok("이미지 업로드 완료!");
    }
}
