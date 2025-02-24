package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListSimpleResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.store.store.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.repository.UserStoreListRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
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
            throw new BusinessException(ErrorCode.INVALID_USER_UUID);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

    /** listId로 특정 리스트 조회 */
    public UserStoreListSimpleResponse getUserStoreList(Long listId) {
        UserStoreList userStoreList = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        return UserStoreListSimpleResponse.builder()
                .listId(userStoreList.getId())
                .listName(userStoreList.getListName())
                .iconColorId(userStoreList.getIconColorId())
                .build();
    }

    /** 저장 리스트 생성 */
    public UserStoreListResponse createUserStoreList(UUID userUuid, String listName, Long iconColorId) {
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_UUID);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean nameExists = userStoreListRepository.existsByUserAndListName(user, listName);
        boolean colorExists = userStoreListRepository.existsByUserAndIconColorId(user, iconColorId);

        if (nameExists && colorExists) {
            throw new BusinessException(ErrorCode.STORE_DUPLICATE_LIST);
        } else if (nameExists) {
            throw new BusinessException(ErrorCode.STORE_DUPLICATE_LIST_NAME);
        } else if (colorExists) {
            throw new BusinessException(ErrorCode.STORE_DUPLICATE_COLOR);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        // 리스트 내 저장된 가게 삭제
        savedStoreRepository.deleteByUserStoreList(list);

        // 리스트 삭제
        userStoreListRepository.deleteById(listId);
    }

    /** 리스트에 가게 추가 */
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<String> userPreferences) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 이미 리스트에 존재하는 가게인지 확인
        boolean exists = savedStoreRepository.findByUserStoreListAndStore(list, store).isPresent();
        if (exists) {
            throw new BusinessException(ErrorCode.STORE_ALREADY_SAVED);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 취향 삭제 후 새로운 취향 저장
        user.getUserPreferences().clear();
        for (String preference : newUserPreferences) {
            PreferenceEntity preferenceEntity = preferenceRepository.findByPreferenceName(preference)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PREFERENCES_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        SavedStore savedStore = savedStoreRepository.findByUserStoreListAndStore(list, store)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVED_STORE_NOT_FOUND));

        savedStoreRepository.delete(savedStore);
    }
}
