package org.swyp.dessertbee.store.store.service;

import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreListLocationResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListSimpleResponse;

import java.util.List;
import java.util.UUID;

public interface UserStoreService {
    /** 저장 리스트 전체 조회 */
    List<UserStoreListResponse> getUserStoreLists(UUID userUuid);

    /** listId로 특정 리스트 조회 */
    UserStoreListSimpleResponse getUserStoreList(Long listId);

    /** 저장 리스트 생성 */
    UserStoreListResponse createUserStoreList(UUID userUuid, String listName, Long iconColorId);

    /** 저장 리스트 수정 */
    UserStoreListResponse updateUserStoreList(Long listId, String newName, Long newIconColorId);

    /** 저장 리스트 삭제 */
    void deleteUserStoreList(Long listId);

    /** 리스트에 가게 추가 */
    SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<Long> userPreferences);

    /** 리스트별 저장된 가게들의 위도, 경도 조회 */
    List<StoreListLocationResponse> getStoresByListId(Long listId);

    /** 리스트별 저장된 가게 조회 */
    UserStoreListResponse getStoresByList(Long listId);

    /** 리스트에서 가게 삭제 */
    void removeStoreFromList(Long listId, UUID storeUuid);
}
