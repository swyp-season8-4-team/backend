package org.swyp.dessertbee.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class ProfileImageUpdateRequestDto {
    private MultipartFile image;
}