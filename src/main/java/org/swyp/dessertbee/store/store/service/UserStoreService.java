package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
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
        long totalLists = lists.size();

        return lists.stream()
                .map(list -> new UserStoreListResponse(
                        userUuid,
                        list.getListName(),
                        list.getIconColorId(),
                        savedStoreRepository.countByUserStoreList(list),
                        totalLists
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

        UserStoreList newList = userStoreListRepository.save(
                UserStoreList.builder()
                        .user(user)
                        .listName(listName)
                        .iconColorId(iconColorId)
                        .build()
        );

        return new UserStoreListResponse(
                userUuid,
                newList.getListName(),
                newList.getIconColorId(),
                0,
                userStoreListRepository.countByUser(user)
        );
    }

    /** 저장 리스트 수정 */
    public UserStoreListResponse updateUserStoreList(Long listId, String newName, Long newIconColorId) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        list.updateList(newName, newIconColorId);
        userStoreListRepository.save(list);

        return new UserStoreListResponse(
                list.getUser().getUserUuid(),
                list.getListName(),
                list.getIconColorId(),
                savedStoreRepository.countByUserStoreList(list),
                userStoreListRepository.countByUser(list.getUser())
        );
    }

    /** 저장 리스트 삭제 */
    public void deleteUserStoreList(Long listId) {
        userStoreListRepository.deleteById(listId);
    }

    /** 리스트에 가게 추가 */
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid) {
        UserStoreList list = userStoreListRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("저장 리스트를 찾을 수 없습니다."));

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new IllegalArgumentException("해당 UUID의 가게를 찾을 수 없습니다: " + storeUuid);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        SavedStore savedStore = savedStoreRepository.save(
                SavedStore.builder()
                        .userStoreList(list)
                        .store(store)
                        .build()
        );

        return new SavedStoreResponse(
                list.getUser().getUserUuid(),
                store.getStoreUuid(),
                list.getListName(),
                store.getName(),
                store.getAddress(),
                imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId())
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
                        list.getListName(),
                        savedStore.getStore().getName(),
                        savedStore.getStore().getAddress(),
                        imageService.getImagesByTypeAndId(ImageType.STORE, savedStore.getStore().getStoreId())
                ))
                .collect(Collectors.toList());
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
