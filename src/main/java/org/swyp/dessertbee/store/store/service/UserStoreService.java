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

import java.util.Collections;
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
                .map(list -> UserStoreListResponse.builder()
                        .listId(list.getId())
                        .userUuid(userUuid)
                        .listName(list.getListName())
                        .iconColorId(list.getIconColorId())
                        .storeCount(savedStoreRepository.countByUserStoreList(list))
                        .storeData(Collections.emptyList())
                        .build())
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

        if (nameExists) {
            throw new BusinessException(ErrorCode.STORE_DUPLICATE_LIST_NAME);
        }

        UserStoreList newList = userStoreListRepository.save(
                UserStoreList.builder()
                        .user(user)
                        .listName(listName)
                        .iconColorId(iconColorId)
                        .build()
        );

        return UserStoreListResponse.builder()
                .listId(newList.getId())
                .userUuid(userUuid)
                .listName(newList.getListName())
                .iconColorId(newList.getIconColorId())
                .storeCount(0)
                .storeData(Collections.emptyList()) // 저장된 가게 정보가 없으므로 빈 리스트 설정
                .build();
    }

    /** 저장 리스트 수정 */
    public UserStoreListResponse updateUserStoreList(Long listId, String newName, Long newIconColorId) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        list.updateList(newName, newIconColorId);
        userStoreListRepository.save(list);

        return UserStoreListResponse.builder()
                .listId(list.getId())
                .userUuid(list.getUser().getUserUuid())
                .listName(list.getListName())
                .iconColorId(list.getIconColorId())
                .storeCount(savedStoreRepository.countByUserStoreList(list))
                .storeData(Collections.emptyList())
                .build();
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
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<Long> userPreferences) {
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
    public UserStoreListResponse getStoresByList(Long listId) {
        // 리스트 엔티티 조회
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_LIST_NOT_FOUND));

        // 해당 리스트에 저장된 가게 정보 매핑
        List<SavedStoreResponse> storeData = savedStoreRepository.findByUserStoreList(list).stream()
                .map(savedStore -> SavedStoreResponse.builder()
                        .userUuid(list.getUser().getUserUuid())
                        .storeUuid(savedStore.getStore().getStoreUuid())
                        .listId(list.getId())
                        .listName(list.getListName())
                        .storeName(savedStore.getStore().getName())
                        .storeAddress(savedStore.getStore().getAddress())
                        .imageUrls(imageService.getImagesByTypeAndId(ImageType.STORE, savedStore.getStore().getStoreId()))
                        .userPreferences(savedStore.getUserPreferences())
                        .build())
                .collect(Collectors.toList());

        int storeCount = storeData.size();

        return UserStoreListResponse.builder()
                .listId(list.getId())
                .userUuid(list.getUser().getUserUuid())
                .listName(list.getListName())
                .iconColorId(list.getIconColorId())
                .storeCount(storeCount)
                .storeData(storeData)
                .build();
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
