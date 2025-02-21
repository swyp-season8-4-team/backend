package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.store.store.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.repository.UserStoreListRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserStoreService {

    private final UserStoreListRepository userStoreListRepository;
    private final SavedStoreRepository savedStoreRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PreferenceRepository preferenceRepository;
    private final ImageService imageService;

    /** 저장 리스트 전체 조회 */
    public List<UserStoreListResponse> getUserStoreLists(UUID userUuid) {
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new IllegalArgumentException("해당 userUuid로 조회된 userId가 없습니다: " + userUuid);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        List<UserStoreList> lists = userStoreListRepository.findByUser(user);

        return lists.stream()
                .map(list -> new UserStoreListResponse(
                        list.getId(),
                        userUuid,
                        list.getListName(),
                        list.getIconColorId(),
                        savedStoreRepository.countByUserStoreList(list)
                ))
                .collect(Collectors.toList());
    }

    /** 저장 리스트 생성 */
    public UserStoreListResponse createUserStoreList(UUID userUuid, String listName, Long iconColorId) {
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new IllegalArgumentException("해당 userUuid로 조회된 userId가 없습니다: " + userUuid);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        boolean nameExists = userStoreListRepository.existsByUserAndListName(user, listName);
        boolean colorExists = userStoreListRepository.existsByUserAndIconColorId(user, iconColorId);

        if (nameExists && colorExists) {
            throw new IllegalArgumentException("동일한 이름과 colorId를 가진 리스트가 이미 존재합니다.");
        } else if (nameExists) {
            throw new IllegalArgumentException("동일한 이름의 리스트가 이미 존재합니다.");
        } else if (colorExists) {
            throw new IllegalArgumentException("동일한 colorId를 가진 리스트가 이미 존재합니다.");
        }

        UserStoreList newList = userStoreListRepository.save(
                UserStoreList.builder()
                        .user(user)
                        .listName(listName)
                        .iconColorId(iconColorId)
                        .build()
        );

        return new UserStoreListResponse(
                newList.getId(),
                userUuid,
                newList.getListName(),
                newList.getIconColorId(),
                0
        );
    }

    /** 저장 리스트 수정 */
    public UserStoreListResponse updateUserStoreList(Long listId, String newName, Long newIconColorId) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        list.updateList(newName, newIconColorId);
        userStoreListRepository.save(list);

        return new UserStoreListResponse(
                list.getId(),
                list.getUser().getUserUuid(),
                list.getListName(),
                list.getIconColorId(),
                savedStoreRepository.countByUserStoreList(list)
        );
    }

    /** 저장 리스트 삭제 */
    public void deleteUserStoreList(Long listId) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        // 리스트 내 저장된 가게 삭제
        savedStoreRepository.deleteByUserStoreList(list);

        // 리스트 삭제
        userStoreListRepository.deleteById(listId);
    }

    /** 리스트에 가게 추가 */
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<String> userPreferences) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new IllegalArgumentException("해당 UUID의 가게를 찾을 수 없습니다: " + storeUuid);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        // 이미 리스트에 존재하는 가게인지 확인
        boolean exists = savedStoreRepository.findByUserStoreListAndStore(list, store).isPresent();
        if (exists) {
            throw new IllegalArgumentException("해당 가게는 이미 리스트에 존재합니다.");
        }

        SavedStore savedStore = savedStoreRepository.save(
                SavedStore.builder()
                        .userStoreList(list)
                        .store(store)
                        .userPreferences(userPreferences)
                        .build()
        );

        return new SavedStoreResponse(
                list.getUser().getUserUuid(),
                store.getStoreUuid(),
                list.getId(),
                list.getListName(),
                store.getName(),
                store.getAddress(),
                imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId()),
                savedStore.getUserPreferences()
        );
    }

    /** 리스트별 저장된 가게 조회 */
    public List<SavedStoreResponse> getStoresByList(Long listId) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        return savedStoreRepository.findByUserStoreList(list).stream()
                .map(savedStore -> new SavedStoreResponse(
                        list.getUser().getUserUuid(),
                        savedStore.getStore().getStoreUuid(),
                        list.getId(),
                        list.getListName(),
                        savedStore.getStore().getName(),
                        savedStore.getStore().getAddress(),
                        imageService.getImagesByTypeAndId(ImageType.STORE, savedStore.getStore().getStoreId()),
                        savedStore.getUserPreferences()
                ))
                .collect(Collectors.toList());
    }

    /** 사용자의 취향을 업데이트하고, 저장된 모든 가게 리스트의 취향도 변경 */
    @Transactional
    public void updateUserPreferencesAndSavedStores(UUID userUuid, List<String> newUserPreferences) {
        Long userId = userRepository.findIdByUserUuid(userUuid);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userUuid));

        // 기존 취향 삭제 후 새로운 취향 저장
        user.getUserPreferences().clear();
        for (String preference : newUserPreferences) {
            PreferenceEntity preferenceEntity = preferenceRepository.findByPreferenceName(preference)
                    .orElseThrow(() -> new IllegalArgumentException("해당 취향이 존재하지 않습니다: " + preference));
            user.getUserPreferences().add(UserPreferenceEntity.builder().user(user).preference(preferenceEntity).build());
        }
        userRepository.save(user); // 변경된 취향 저장

        // 사용자의 저장 리스트 가져오기
        List<UserStoreList> userLists = userStoreListRepository.findByUser(user);
        if (userLists.isEmpty()) return; // 저장 리스트가 없으면 종료

        // 저장 리스트에 속한 모든 가게 찾기
        List<SavedStore> savedStores = savedStoreRepository.findByUserStoreListIn(userLists);
        if (savedStores.isEmpty()) return; // 저장된 가게가 없으면 종료

        // 모든 가게의 취향을 새로 업데이트
        for (SavedStore savedStore : savedStores) {
            savedStore.getUserPreferences().clear();
            savedStore.setUserPreferences(newUserPreferences);
        }

        savedStoreRepository.saveAll(savedStores);
    }

    /** 리스트에서 가게 삭제 */
    public void removeStoreFromList(Long listId, UUID storeUuid) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new IllegalArgumentException("해당 UUID의 가게를 찾을 수 없습니다: " + storeUuid);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        SavedStore savedStore = savedStoreRepository.findByUserStoreListAndStore(list, store)
                .orElseThrow(() -> new IllegalArgumentException("해당 리스트에 저장된 가게가 없습니다."));

        savedStoreRepository.delete(savedStore);
    }
}
