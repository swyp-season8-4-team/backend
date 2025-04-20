package org.swyp.dessertbee.store.saved.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.statistics.store.entity.enums.SaveAction;
import org.swyp.dessertbee.statistics.store.event.StoreSaveActionEvent;
import org.swyp.dessertbee.store.saved.dto.*;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.store.saved.exception.UserStoreExceptions.*;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.saved.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.saved.entity.UserStoreList;
import org.swyp.dessertbee.store.saved.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.saved.repository.UserStoreListRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserStoreServiceImpl implements UserStoreService {

    private final UserStoreListRepository userStoreListRepository;
    private final SavedStoreRepository savedStoreRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    /** 저장 리스트 전체 조회 */
    @Override
    public List<UserStoreListSummaryResponse> getUserStoreLists(UUID userUuid) {
        try {
            Long userId = userRepository.findIdByUserUuid(userUuid);
            if (userId == null) {
                throw new InvalidUserUuidException();
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);

            List<UserStoreList> lists = userStoreListRepository.findByUser(user);
            List<StoreListLocationResponse> allLocations = savedStoreRepository.findAllLocationByUserId(userId);

            Map<Long, List<StoreListLocationResponse>> grouped = allLocations.stream()
                    .collect(Collectors.groupingBy(StoreListLocationResponse::getListId));

            return lists.stream()
                    .map(list -> UserStoreListSummaryResponse.builder()
                            .listId(list.getId())
                            .userUuid(userUuid)
                            .listName(list.getListName())
                            .iconColorId(list.getIconColorId())
                            .storeCount(grouped.getOrDefault(list.getId(), List.of()).size())
                            .storeData(grouped.getOrDefault(list.getId(), List.of()))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("저장 리스트 전체 조회 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 전체 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** listId로 특정 리스트 조회 */
    @Override
    public UserStoreListSimpleResponse getUserStoreList(Long listId) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

            return UserStoreListSimpleResponse.builder()
                    .listId(list.getId())
                    .listName(list.getListName())
                    .iconColorId(list.getIconColorId())
                    .build();
        } catch (Exception e) {
            log.error("저장 리스트 생성 처리 중 오류 발생", e);
            throw new UserStoreServiceException("저장 리스트 생성 처리 중 오류가 발생했습니다.");
        }
    }

    /** 저장 리스트 생성 */
    @Override
    public UserStoreListSummaryResponse createUserStoreList(UUID userUuid, String listName, Long iconColorId) {
        try{
            Long userId = userRepository.findIdByUserUuid(userUuid);
            if (userId == null) {
                throw new InvalidUserUuidException();
            }


            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);

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

            return UserStoreListSummaryResponse.builder()
                    .listId(newList.getId())
                    .userUuid(userUuid)
                    .listName(newList.getListName())
                    .iconColorId(newList.getIconColorId())
                    .storeCount(0)
                    .storeData(Collections.emptyList())
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
    @Override
    public UserStoreListSummaryResponse updateUserStoreList(Long listId, String newName, Long newIconColorId) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

            list.updateList(newName, newIconColorId);
            userStoreListRepository.save(list);

            return UserStoreListSummaryResponse.builder()
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
    @Override
    public void deleteUserStoreList(Long listId) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

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
    @Override
    public SavedStoreResponse addStoreToList(Long listId, UUID storeUuid, List<Long> userPreferences) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(StoreNotFoundException::new);

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

            UserEntity user = userService.getCurrentUser();

            eventPublisher.publishEvent(new StoreSaveActionEvent(storeId, user.getUserUuid(), SaveAction.SAVE));

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
    @Override
    public List<StoreListLocationResponse> getStoresByListId(Long listId) {
        return savedStoreRepository.findStoresByListId(listId);
    }

    /** 리스트별 저장된 가게 조회 */
    @Override
    public UserStoreListDetailResponse getStoresByList(Long listId) {
        try{
            // 리스트 엔티티 조회
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

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

            return UserStoreListDetailResponse.builder()
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
    @Override
    public void removeStoreFromList(Long listId, UUID storeUuid) {
        try{
            UserStoreList list = userStoreListRepository.findById(listId)
                    .orElseThrow(StoreListNotFoundException::new);

            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(StoreNotFoundException::new);

            SavedStore savedStore = savedStoreRepository.findByUserStoreListAndStore(list, store)
                    .orElseThrow(SavedStoreNotFoundException::new);

            UserEntity user = userService.getCurrentUser();

            eventPublisher.publishEvent(new StoreSaveActionEvent(storeId, user.getUserUuid(), SaveAction.UNSAVE));

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
