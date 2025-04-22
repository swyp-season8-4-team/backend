package org.swyp.dessertbee.store.store.service;

import com.nimbusds.jose.util.Pair;
import org.swyp.dessertbee.community.mate.dto.response.MateResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewSummaryResponse;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.preference.dto.TopPreferenceTagResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

public interface StoreSupportService {
    List<StoreReviewResponse> getStoreReviewResponses(Long storeId);
    List<ReviewSummaryResponse> getCommunityReviewResponses(Long storeId);
    List<MateResponse> getMateResponses(Long storeId, Long userId);
    List<TopPreferenceTagResponse> getTop3Preferences(Long storeId);
    Pair<Boolean, Long> getUserStoreSavedInfo(Store store, UserEntity user);
}
