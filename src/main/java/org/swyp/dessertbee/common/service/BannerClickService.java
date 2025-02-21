package org.swyp.dessertbee.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.BannerClick;
import org.swyp.dessertbee.common.repository.BannerClickRepository;

@Service
@RequiredArgsConstructor
public class BannerClickService {

    private final BannerClickRepository bannerClickRepository;

    @Transactional
    public void increaseBannerClick() {
        BannerClick bannerClick = bannerClickRepository.findById(1L)
                .orElseGet(() -> bannerClickRepository.save(BannerClick.builder().clickCount(0).build()));

        bannerClick.incrementClickCount();
        bannerClickRepository.save(bannerClick);
    }

    @Transactional(readOnly = true)
    public int getBannerClickCount() {
        return bannerClickRepository.findById(1L)
                .map(BannerClick::getClickCount)
                .orElse(0);
    }
}
