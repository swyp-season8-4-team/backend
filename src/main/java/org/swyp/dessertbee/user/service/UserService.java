package org.swyp.dessertbee.user.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;
import org.swyp.dessertbee.user.dto.UserUpdateRequestDto;
import org.swyp.dessertbee.user.entity.NicknameValidationPurpose;

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


    /**
     * 현재 인증된 사용자의 정보를 수정합니다.
     * @return 사용자 상세 정보 DTO
     */
    UserDetailResponseDto updateMyInfo(UserUpdateRequestDto updateRequest);

    /**
     * 현재 인증된 사용자의 계정을 비활성화(소프트 삭제)합니다.
     */
    void deleteMyAccount();

    /**
     * 닉네임 중복 여부를 확인합니다.
     * @param nickname 검사할 닉네임
     * @return 사용 가능 여부
     */
    boolean checkNicknameAvailability(String nickname, NicknameValidationPurpose purpose);

    /**
     * @param image 새로운 프로필 이미지 파일
     * @return 업데이트된 사용자 정보
     */
    UserDetailResponseDto updateProfileImage(MultipartFile image);

}
