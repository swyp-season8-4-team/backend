package org.swyp.dessertbee.store.link.service;

import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.dto.request.BaseStoreRequest;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.link.entity.StoreLink;
import org.swyp.dessertbee.store.store.exception.StoreExceptions;
import org.swyp.dessertbee.store.link.repository.StoreLinkRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreLinkServiceImpl implements StoreLinkService {

    private StoreLinkRepository storeLinkRepository;

    /**
     * 링크 유효성 검사 및 저장 메서드
     * List<? extends StoreLinkRequest> 타입으로 변경하여 제네릭 타입 호환성 문제 해결
     */
    @Override
    public void validateAndSaveStoreLinks(Store store, List<? extends BaseStoreRequest.StoreLinkRequest> linkRequests) {
        if (linkRequests != null && !linkRequests.isEmpty()) {
            // 기본 링크 중복 체크
            long primaryCount = linkRequests.stream()
                    .filter(link -> Boolean.TRUE.equals(link.getIsPrimary()))
                    .count();

            if (primaryCount > 1) {
                throw new StoreExceptions.DuplicatePrimaryLinkException();
            }

            List<StoreLink> links = linkRequests.stream()
                    .map(linkReq -> StoreLink.builder()
                            .storeId(store.getStoreId())
                            .url(linkReq.getUrl())
                            .isPrimary(Boolean.TRUE.equals(linkReq.getIsPrimary()))
                            .build())
                    .toList();

            storeLinkRepository.saveAll(links);
        }
    }

    /**
     * 가게 링크 조회 및 대표 링크 추출 메서드
     */
    @Override
    public Pair<List<String>, String> getStoreLinksAndPrimary(Long storeId) {
        List<StoreLink> storeLinks = storeLinkRepository.findByStoreId(storeId);

        String primaryStoreLink = storeLinks.stream()
                .filter(StoreLink::getIsPrimary)
                .map(StoreLink::getUrl)
                .findFirst()
                .orElse(null);

        List<String> linkUrls = storeLinks.stream()
                .map(StoreLink::getUrl)
                .toList();

        return Pair.of(linkUrls, primaryStoreLink);
    }
}
