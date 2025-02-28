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
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.store.store.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.repository.UserStoreListRepository;
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
    private final UserStoreListRepository userStoreListRepository;
    private final SavedStoreRepository savedStoreRepository;

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
     * 사용자의 선호도 설정 여부를 확인합니다.
     * @param user 확인할 사용자 엔티티
     * @return 선호도 설정 여부 (설정된 경우 true)
     */
    public boolean isUserPreferenceSet(UserEntity user) {
        // 사용자의 선호도 설정이 존재하고 비어있지 않은 경우 true 반환
        return user.getUserPreferences() != null && !user.getUserPreferences().isEmpty();
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

        // 유저가 저장한 가게(SavedStore)의 선호도도 업데이트
        updateSavedStoresPreferences(user, preferences);
    }

    /**
     * 유저가 저장한 가게의 선호도 정보를 업데이트합니다.
     */
    private void updateSavedStoresPreferences(UserEntity user, Set<PreferenceEntity> newPreferences) {
        List<UserStoreList> userLists = userStoreListRepository.findByUser(user);
        if (userLists.isEmpty()) return;

        // 유저의 저장된 모든 가게 가져오기
        List<SavedStore> savedStores = savedStoreRepository.findByUserStoreListIn(userLists);
        if (savedStores.isEmpty()) return;

        // 모든 저장된 가게의 선호도를 새로운 선호도로 업데이트
        for (SavedStore savedStore : savedStores) {
            savedStore.getUserPreferences().clear();
            savedStore.setUserPreferences(newPreferences.stream().map(PreferenceEntity::getPreferenceName).toList());
        }

        // 변경된 가게 선호도 저장
        savedStoreRepository.saveAll(savedStores);
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