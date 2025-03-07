package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.preference.service.PreferenceService;
import org.swyp.dessertbee.user.dto.response.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.response.UserResponseDto;
import org.swyp.dessertbee.user.dto.request.UserUpdateRequestDto;
import org.swyp.dessertbee.user.entity.MbtiEntity;
import org.swyp.dessertbee.user.entity.NicknameValidationPurpose;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.MbtiRepository;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * UserService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MbtiRepository mbtiRepository;
    private final ImageService imageService;
    private final PreferenceService preferenceService;




    /**
     * Security Context에서 현재 인증된 사용자의 정보를 조회합니다.
     * 비로그인 상태인 경우 null 반환
     */
    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            log.warn("SecurityContext에 인증 정보가 없습니다.");
            return null;
        }
        log.debug("현재 인증된 사용자: {}", authentication.getName());

        String email = authentication.getName();

        return userRepository.findByEmail(email).orElse(null);
    }



    /**
     * 현재 인증된 사용자의 상세 정보를 조회합니다.
     */
    @Override
    public UserDetailResponseDto getMyUserInfo() {
        UserEntity user = getCurrentUser();

        return convertToDetailResponse(user);
    }

    /**
     * UUID로 사용자의 기본 정보를 조회합니다.
     */
    @Override
    public UserResponseDto getUserInfo(String userUuid) {
        try {
            UUID uuid = UUID.fromString(userUuid);
            Long userId = userRepository.findIdByUserUuid(uuid);

            if (userId == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            return convertToResponse(user);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_USER_UUID);
        }
    }

    /**
     * 프로필 이미지 업데이트 구현
     * 기존 코드에 새로 추가되는 메서드입니다.
     */
    @Override
    @Transactional
    public UserDetailResponseDto updateProfileImage(MultipartFile image) {
        // 입력값 검증
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일이 필요합니다.");
        }

        // 현재 사용자 조회
        UserEntity user = getCurrentUser();

        // S3 폴더 경로 설정 - "profile/[userId]" 형식
        String folder = String.format("profile/%d", user.getId());

        try {
            // 이미지 서비스를 통해 기존 이미지 교체
            // updateImage 메서드는 기존 이미지가 있다면 삭제하고 새 이미지를 업로드
            imageService.updateImage(ImageType.PROFILE, user.getId(), image, folder);

            log.info("프로필 이미지 업데이트 성공 - userId: {}", user.getId());

            // 업데이트된 사용자 정보 반환
            return convertToDetailResponse(user);

        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패 - userId: {}", user.getId(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "프로필 이미지 업데이트에 실패했습니다.");
        }
    }

    /**
     * UserEntity를 UserDetailResponseDto로 변환합니다.
     */
    private UserDetailResponseDto convertToDetailResponse(UserEntity user) {

        // 프로필 이미지 URL 조회
        List<String> profileImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());
        String profileImageUrl = profileImages.isEmpty() ? null : profileImages.get(0);


        return UserDetailResponseDto.detailBuilder()
                .userUuid(user.getUserUuid().toString())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .gender(user.getGender())
                .profileImage(profileImageUrl)  // imageId 대신 imageUrl 사용
                .preferences(preferenceService.convertToPreferenceIds(user.getUserPreferences()))
                .mbti(user.getMbti() != null ? user.getMbti().getMbtiType() : null)
                .build();
    }

    /**
     * UserEntity를 UserResponseDto로 변환합니다.
     */
    private UserResponseDto convertToResponse(UserEntity user) {
        // 프로필 이미지 URL 조회
        List<String> profileImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());
        String profileImageUrl = profileImages.isEmpty() ? null : profileImages.get(0);

        return UserResponseDto.builder()
                .userUuid(user.getUserUuid().toString())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .profileImageUrl(profileImageUrl)
                .preferences(preferenceService.convertToPreferenceIds(user.getUserPreferences()))
                .mbti(user.getMbti() != null ? user.getMbti().getMbtiType() : null)
                .build();
    }

    /**
     * 닉네임 중복을 검사합니다.
     * @param currentUser 현재 사용자
     * @param newNickname 변경하려는 닉네임
     * @throws BusinessException 닉네임이 중복된 경우
     */
    private void validateNickname(UserEntity currentUser, String newNickname) {
        if (newNickname == null) {
            return;
        }

        // 현재 닉네임과 동일한 경우 검사 스킵
        if (newNickname.equals(currentUser.getNickname())) {
            return;
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(newNickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Override
    @Transactional
    public UserDetailResponseDto updateMyInfo(UserUpdateRequestDto updateRequest) {
        UserEntity user = getCurrentUser();

        // 닉네임 중복 검사
        validateNickname(user, updateRequest.getNickname());

        // 기본 정보 업데이트
        updateBasicInfo(user, updateRequest);

        // 선호도 업데이트
        if (updateRequest.getPreferences() != null) {
            preferenceService.updateUserPreferences(user, updateRequest.getPreferences());
        }

        // MBTI 업데이트
        if (updateRequest.getMbti() != null) {
            updateMbti(user, updateRequest.getMbti());
        }

        userRepository.save(user);

        return convertToDetailResponse(user);
    }

    /**
     * 사용자의 기본 정보를 업데이트합니다.
     * 전달받은 필드 중 null이 아닌 값만 업데이트합니다.
     */
    private void updateBasicInfo(UserEntity user, UserUpdateRequestDto updateRequest) {
        Optional.ofNullable(updateRequest.getName())
                .ifPresent(user::setName);

        Optional.ofNullable(updateRequest.getNickname())
                .ifPresent(user::setNickname);

        Optional.ofNullable(updateRequest.getPhoneNumber())
                .ifPresent(user::setPhoneNumber);

        Optional.ofNullable(updateRequest.getAddress())
                .ifPresent(user::setAddress);

        Optional.ofNullable(updateRequest.getGender())
                .ifPresent(user::setGender);
    }

    /**
     * 사용자의 MBTI 정보를 업데이트
     * null이 전달된 경우 MBTI 정보를 제거
     */
    private void updateMbti(UserEntity user, String mbtiType) {
        if (mbtiType == null) {
            user.setMbti(null);
            return;
        }

        MbtiEntity mbti = mbtiRepository.findByMbtiType(mbtiType.toUpperCase())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "유효하지 않은 MBTI 타입입니다: " + mbtiType
                ));

        user.setMbti(mbti);
    }

    /**
     * 현재 인증된 사용자의 계정을 비활성화합니다.
     */
    @Override
    @Transactional
    public void deleteMyAccount() {
        UserEntity user = getCurrentUser();
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // 연관된 인증 정보도 비활성화
        user.getAuths().forEach(AuthEntity::deactivate);

        log.info("해당 유저의 계정이 비활성화 처리 되었습니다 : {}", user.getEmail());
    }

    @Override
    public boolean checkNicknameAvailability(String nickname, NicknameValidationPurpose purpose) {
        // 프로필 수정의 경우 인증 확인
        if (purpose == NicknameValidationPurpose.PROFILE_UPDATE) {
            UserEntity currentUser = getCurrentUser();
            // 현재 사용자의 닉네임과 동일한 경우 true 반환
            if (currentUser.getNickname().equals(nickname)) {
                return true;
            }
        }

        return !userRepository.existsByNickname(nickname);
    }

    /**
     * AuthenticationPrincipal 을 위해 email로 user 조회
     * */
    @Override
    public UserEntity validateUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자 조회
     */
    @Override
    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", email);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
    }
}