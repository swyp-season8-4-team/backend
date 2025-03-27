package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.store.store.exception.UserStoreExceptions.*;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreListLocationResponse;
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

@Slf4j
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
        try{
            Long userId = userRepository.findIdByUserUuid(userUuid);
            if (userId == null) {
                throw new InvalidUserUuidException();
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException());

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
        } catch (Exception e) {
            log.error("저장 리스트 생성 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 생성 처리 중 오류가 발생했습니다.");
        }
    }

    /** listId로 특정 리스트 조회 */
    public UserStoreListSimpleResponse getUserStoreList(Long listId) {
        try{
            UserStoreList userStoreList = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

            return UserStoreListSimpleResponse.builder()
                    .listId(userStoreList.getId())
                    .listName(userStoreList.getListName())
                    .iconColorId(userStoreList.getIconColorId())
                    .build();
        } catch (Exception e) {
            log.error("저장 리스트 생성 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 생성 처리 중 오류가 발생했습니다.");
        }
    }

    /** 저장 리스트 생성 */
    public UserStoreListResponse createUserStoreList(UUID userUuid, String listName, Long iconColorId) {
        try{
            Long userId = userRepository.findIdByUserUuid(userUuid);
            if (userId == null) {
                throw new InvalidUserUuidException();
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException());

            boolean nameExists = userStoreListRepository.existsByUserAndListName(user, listName);

            if (nameExists) {
                throw new DuplicateListNameException();
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
        } catch (ListCreationFailedException e){
            log.warn("저장 리스트 생성 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("저장 리스트 생성 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 생성 처리 중 오류가 발생했습니다.");
        }
    }

    /** 저장 리스트 수정 */
    public UserStoreListResponse updateUserStoreList(Long listId, String newName, Long newIconColorId) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

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
        } catch (ListUpdateFailedException e){
            log.warn("저장 리스트 수정 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("저장 리스트 수정 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 수정 처리 중 오류가 발생했습니다.");
        }
    }

    /** 저장 리스트 삭제 */
    public void deleteUserStoreList(Long listId) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

            // 리스트 내 저장된 가게 삭제
            savedStoreRepository.deleteByUserStoreList(list);

            // 리스트 삭제
            userStoreListRepository.deleteById(listId);
        } catch (ListDeleteFailedException e){
            log.warn("저장 리스트 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("저장 리스트 삭제 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    /** 리스트에 가게 추가 */
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<Long> userPreferences) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            // 이미 리스트에 존재하는 가게인지 확인
            boolean exists = savedStoreRepository.findByUserStoreListAndStore(list, store).isPresent();
            if (exists) {
                throw new DuplicateStoreSaveException();
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
                    store.getLatitude(),
                    store.getLongitude(),
                    imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId()),
                    savedStore.getUserPreferences()
            );
        } catch (StoreSaveException e){
            log.warn("리스트에 가게 저장 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리스트에 가게 저장 처리 중 오류 발생", e);
            throw new UserStoreServiceException("리스트에 가게 저장 처리 중 오류가 발생했습니다.");
        }
    }

    /** 리스트별 저장된 가게들의 위도, 경도 조회 */
    public List<StoreListLocationResponse> getStoresByListId(Long listId) {
        return savedStoreRepository.findStoresByListId(listId);
    }

    /** 리스트별 저장된 가게 조회 */
    public UserStoreListResponse getStoresByList(Long listId) {
        try{
            // 리스트 엔티티 조회
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

            // 해당 리스트에 저장된 가게 정보 매핑
            List<SavedStoreResponse> storeData = savedStoreRepository.findByUserStoreList(list).stream()
                    .map(savedStore -> SavedStoreResponse.builder()
                            .userUuid(list.getUser().getUserUuid())
                            .storeUuid(savedStore.getStore().getStoreUuid())
                            .listId(list.getId())
                            .listName(list.getListName())
                            .storeName(savedStore.getStore().getName())
                            .storeAddress(savedStore.getStore().getAddress())
                            .latitude(savedStore.getStore().getLatitude())
                            .longitude(savedStore.getStore().getLongitude())
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
        } catch (Exception e) {
            log.error("리스트에 저장된 가게 조회 처리 중 오류 발생", e);
            throw new UserStoreServiceException("리스트에 저장된 가게 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 리스트에서 가게 삭제 */
    public void removeStoreFromList(Long listId, UUID storeUuid) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(() -> new StoreListNotFoundException());

            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException());

            SavedStore savedStore = savedStoreRepository.findByUserStoreListAndStore(list, store)
                    .orElseThrow(() -> new SavedStoreNotFoundException());

            savedStoreRepository.delete(savedStore);
        } catch (SavedStoreDeleteException e){
            log.warn("리스트에 가게 저장 취소 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("리스트에 가게 저장 취소 처리 중 오류 발생", e);
            throw new UserStoreServiceException("리스트에 가게 저장 취소 처리 중 오류가 발생했습니다.");
        }
    }
}
