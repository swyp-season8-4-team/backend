package org.swyp.dessertbee.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Security Context에서 현재 인증된 사용자의 정보를 조회합니다.
     */
    private UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
     * UserEntity를 UserDetailResponseDto로 변환합니다.
     */
    private UserDetailResponseDto convertToDetailResponse(UserEntity user) {
        return UserDetailResponseDto.detailBuilder()
                .userUuid(user.getUserUuid().toString())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .gender(user.getGender())
                .imageId(user.getImageId())
                .preferences(convertToPreferenceIds(user.getUserPreferences()))
                .mbti(user.getMbti() != null ? user.getMbti().getMbtiType() : null)
                .build();
    }

    /**
     * UserEntity를 UserResponseDto로 변환합니다.
     */
    private UserResponseDto convertToResponse(UserEntity user) {
        return UserResponseDto.builder()
                .userUuid(user.getUserUuid().toString())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .imageId(user.getImageId())
                .preferences(convertToPreferenceIds(user.getUserPreferences()))
                .mbti(user.getMbti() != null ? user.getMbti().getMbtiType() : null)
                .build();
    }

    /**
     * UserPreferenceEntity Set을 preference ID List로 변환합니다.
     */
    private List<Long> convertToPreferenceIds(Set<UserPreferenceEntity> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return Collections.emptyList();
        }
        return preferences.stream()
                .map(up -> up.getPreference().getId())
                .collect(Collectors.toList());
    }
}