package org.swyp.dessertbee.store.store.service;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.response.MateResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateCategory;
import org.swyp.dessertbee.community.mate.entity.SavedMate;
import org.swyp.dessertbee.community.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.community.mate.repository.SavedMateRepository;
import org.swyp.dessertbee.community.review.dto.response.ReviewSummaryResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewContent;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.store.preference.dto.TopPreferenceTagResponse;
import org.swyp.dessertbee.store.preference.repository.StoreTopTagRepository;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.saved.entity.SavedStore;
import org.swyp.dessertbee.store.saved.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.entity.*;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserBlockService;
import org.swyp.dessertbee.user.service.UserService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreSupportServiceImpl implements StoreSupportService{
    private final StoreReviewRepository storeReviewRepository;
    private final ReviewRepository communityReviewRepository;
    private final StoreTopTagRepository storeTopTagRepository;
    private final SavedStoreRepository savedStoreRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final MateRepository mateRepository;
    private final MateCategoryRepository mateCategoryRepository;
    private final SavedMateRepository savedMateRepository;
    private final UserService userService;
    private final UserBlockService userBlockService;

    /**
     * 가게의 Top3 취향 태그 조회 메서드
     */
    @Override
    public List<TopPreferenceTagResponse> getTop3Preferences(Long storeId) {
        return storeTopTagRepository.findTop3TagsByStoreId(storeId);
    }

    /**
     * 사용자의 가게 저장 정보 조회 메서드
     */
    @Override
    public Pair<Boolean, Long> getUserStoreSavedInfo(Store store, UserEntity user) {
        if (user == null) {
            return Pair.of(false, null);
        }

        List<SavedStore> savedStores = savedStoreRepository.findByStoreAndUserId(store, user.getId());
        boolean saved = !savedStores.isEmpty();
        Long savedListId = savedStores.isEmpty() ? null : savedStores.get(0).getUserStoreList().getId();

        log.info("사용자가 가게를 저장했는지 여부: {}, savedListId: {}", saved, savedListId);
        return Pair.of(saved, savedListId);
    }

    /**
     * 가게 한줄 리뷰 조회 및 변환 메서드
     */
    @Override
    public List<StoreReviewResponse> getStoreReviewResponses(Long storeId) {
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        UserEntity currentUser = userService.getCurrentUser();
        final List<UUID> blockedUserUuids = currentUser != null
                ? userBlockService.getBlockedUserUuids(currentUser.getUserUuid())
                : Collections.emptyList();

        Map<Long, List<String>> reviewImagesMap = imageService.getImagesByTypeAndIds(ImageType.SHORT,
                reviews.stream().map(StoreReview::getReviewId).toList());

        return reviews.stream()
                .filter(review -> !blockedUserUuids.contains(review.getUserUuid())) // 차단한 사용자 필터링
                .map(review -> {
                    Long reviewerId = userRepository.findIdByUserUuid(review.getUserUuid());
                    UserEntity reviewer = userRepository.findById(reviewerId)
                            .orElseThrow(() -> new UserExceptions.UserNotFoundException());
                    List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

                    return StoreReviewResponse.fromEntity(review, reviewer,
                            profileImage.isEmpty() ? null : profileImage.get(0),
                            reviewImagesMap.getOrDefault(review.getReviewId(), Collections.emptyList()));
                }).toList();
    }

    /**
     * 가게 커뮤니티 리뷰 조회 및 변환 메서드
     */
    @Override
    public List<ReviewSummaryResponse> getCommunityReviewResponses(Long storeId) {
        List<Review> communityReviews = communityReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        UserEntity currentUser = userService.getCurrentUser();
        final List<UUID> blockedUserUuids = currentUser != null
                ? userBlockService.getBlockedUserUuids(currentUser.getUserUuid())
                : Collections.emptyList();

        return communityReviews.stream()
                .filter(review -> {
                    UserEntity reviewer = userRepository.findById(review.getUserId())
                            .orElse(null);
                    return reviewer != null && !blockedUserUuids.contains(reviewer.getUserUuid());
                })
                .map(review -> {
                    UserEntity reviewer = userRepository.findById(review.getUserId())
                            .orElseThrow(() -> new UserExceptions.UserNotFoundException());

                    List<String> profileImageList = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());
                    String profileImage = profileImageList.isEmpty() ? null : profileImageList.get(0);

                    String thumbnail = null;
                    String content = "";

                    for (ReviewContent contentItem : review.getReviewContents()) {
                        if (thumbnail == null && "image".equals(contentItem.getType())) {
                            thumbnail = contentItem.getValue();
                        } else if (content.isEmpty() && "text".equals(contentItem.getType())) {
                            content = contentItem.getValue();
                        }

                        if (thumbnail != null && !content.isEmpty()) {
                            break;
                        }
                    }

                    return ReviewSummaryResponse.builder()
                            .reviewUuid(review.getReviewUuid())
                            .userUuid(reviewer.getUserUuid())
                            .nickname(reviewer.getNickname())
                            .profileImage(profileImage)
                            .thumbnail(thumbnail)
                            .title(review.getTitle())
                            .content(content)
                            .createdAt(review.getCreatedAt())
                            .updatedAt(review.getUpdatedAt())
                            .build();
                }).toList();
    }

    /**
     * 디저트 메이트 조회 및 변환 메서드
     */
    @Override
    public List<MateResponse> getMateResponses(Long storeId, Long userId) {
        List<Mate> mates = mateRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        return mates.stream().map(mate -> {
            UserEntity mateCreator = userRepository.findById(mate.getUserId())
                    .orElseThrow(() -> new UserExceptions.UserNotFoundException());
            String mateCategory = mateCategoryRepository.findById(mate.getMateCategoryId())
                    .map(MateCategory::getName).orElse("알 수 없음");
            List<String> mateThumbnail = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());

            //저장했는지 유무 확인
            SavedMate savedMate = userId != null ?
                    savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId) : null;
            boolean mateSaved = (savedMate != null);

            return MateResponse.builder()
                    .mateUuid(mate.getMateUuid())
                    .mateCategory(mateCategory)
                    .thumbnail(mateThumbnail.isEmpty() ? null : mateThumbnail.get(0))
                    .title(mate.getTitle())
                    .content(mate.getContent())
                    .nickname(mateCreator.getNickname())
                    .recruitYn(mate.getRecruitYn())
                    .saved(mateSaved)
                    .build();
        }).toList();
    }
}
