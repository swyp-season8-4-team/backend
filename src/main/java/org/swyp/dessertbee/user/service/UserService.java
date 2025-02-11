package org.swyp.dessertbee.user.service;

import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;

public interface UserService {
    /**
     * 현재 인증된 사용자의 상세 정보를 조회
     * @return 사용자 상세 정보 DTO
     */
    UserDetailResponseDto getMyUserInfo();

    /**
     * UUID로 특정 사용자의 기본 정보를 조회
     * @param userUuid 조회할 사용자의 UUID
     * @return 사용자 기본 정보 DTO
     */
    UserResponseDto getUserInfo(String userUuid);
}

