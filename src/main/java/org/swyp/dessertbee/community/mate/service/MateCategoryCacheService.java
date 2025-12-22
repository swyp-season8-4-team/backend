package org.swyp.dessertbee.community.mate.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.community.mate.repository.MateCategoryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MateCategoryCacheService {

    private final MateCategoryRepository mateCategoryRepository;

    @Cacheable(value = "mateCategory", key = "#categoryId")
    public String findCategoryNameById(Long categoryId) {
        return mateCategoryRepository.findCategoryNameById(categoryId);
    }

    @PostConstruct
    public void warmUpCache() {
        log.info("MateCategory 캐시 워밍업 시작");
        // 카테고리 개수가 적으므로 필요 시 전체 로드 가능
    }
}
