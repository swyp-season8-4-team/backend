package org.swyp.dessertbee.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.entity.MbtiEntity;
import org.swyp.dessertbee.user.repository.MbtiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MbtiCacheService {

    private final MbtiRepository mbtiRepository;

    @Cacheable(value = "mbti", key = "#mbtiType")
    public MbtiEntity findByMbtiType(String mbtiType) {
        return mbtiRepository.findByMbtiType(mbtiType.toUpperCase())
            .orElseThrow(() -> new BusinessException(ErrorCode.MBTI_NOT_FOUND));
    }

    @PostConstruct
    public void warmUpCache() {
        List<MbtiEntity> allMbti = mbtiRepository.findAll();
        allMbti.forEach(mbti -> findByMbtiType(mbti.getMbtiType()));
        log.info("MBTI 캐시 워밍업 완료: {} 개", allMbti.size());
    }
}
