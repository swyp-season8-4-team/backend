package org.swyp.dessertbee.preference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.preference.dto.PreferenceResponseDto;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 조회 전용 트랜잭션으로 설정하여 성능 최적화
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;

    /**
     * 모든 선호도 정보를 조회하는 메서드
     * @return 모든 선호도 정보가 담긴 DTO 리스트
     */
    public List<PreferenceResponseDto> getAllPreferences() {
        return preferenceRepository.findAll()  // 모든 선호도 엔티티 조회
                .stream()
                .map(preference -> PreferenceResponseDto.builder()  // 엔티티를 DTO로 변환
                        .id(preference.getId())
                        .preferenceName(preference.getPreferenceName())
                        .preferenceDesc(preference.getPreferenceDesc())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 선호도 정보를 업데이트합니다.
     * 기존 선호도를 모두 제거하고 새로운 선호도로 대체합니다.
     */
    @Transactional
    public void updateUserPreferences(UserEntity user, List<Long> newPreferenceIds) {
        // 기존 선호도 모두 제거
        user.getUserPreferences().clear();

        // 새로운 선호도가 없는 경우 종료
        if (newPreferenceIds == null || newPreferenceIds.isEmpty()) {
            return;
        }

        // 새로운 선호도 ID들의 유효성을 한 번에 검사
        Set<PreferenceEntity> preferences = new HashSet<>(preferenceRepository.findAllById(newPreferenceIds));

        // 요청된 모든 선호도 ID가 유효한지 확인
        if (preferences.size() != newPreferenceIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 선호도가 포함되어 있습니다.");
        }

        // 새로운 선호도 설정
        preferences.forEach(preference -> {
            UserPreferenceEntity userPreference = UserPreferenceEntity.builder()
                    .user(user)
                    .preference(preference)
                    .build();
            user.getUserPreferences().add(userPreference);
        });
    }

    /**
     * UserPreferenceEntity Set을 preference ID List로 변환합니다.
     */
    public List<Long> convertToPreferenceIds(Set<UserPreferenceEntity> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return preferences.stream()
                .map(up -> up.getPreference().getId())
                .collect(Collectors.toList());
    }

}