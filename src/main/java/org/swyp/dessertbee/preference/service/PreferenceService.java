package org.swyp.dessertbee.preference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.preference.dto.PreferenceResponseDto;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 조회 전용 트랜잭션으로 설정하여 성능 최적화
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;

    /**
     * 모든 선호도 정보를 조회하는 메서드
     * @return 모든 선호도 정보가 담긴 DTO 리스트
     */
    public List<PreferenceResponseDto> getAllPreferences() {
        return preferenceRepository.findAll()  // 모든 선호도 엔티티 조회
                .stream()
                .map(preference -> PreferenceResponseDto.builder()  // 엔티티를 DTO로 변환
                        .id(preference.getId())
                        .preferenceName(preference.getPreferenceName())
                        .preferenceDesc(preference.getPreferenceDesc())
                        .build())
                .collect(Collectors.toList());
    }
}