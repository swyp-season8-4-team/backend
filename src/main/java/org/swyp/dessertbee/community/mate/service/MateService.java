package org.swyp.dessertbee.community.mate.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.community.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;

public interface MateService {


    MateDetailResponse createMate(MateCreateRequest request, MultipartFile mateImage);
}
