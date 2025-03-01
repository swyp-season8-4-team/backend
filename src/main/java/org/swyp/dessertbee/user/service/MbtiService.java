package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.user.dto.response.MbtiResponseDto;
import org.swyp.dessertbee.user.repository.MbtiRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiService {

    private final MbtiRepository mbtiRepository;

    /**
     * 모든 MBTI 정보를 조회하는 메서드
     *
     * @return 모든 MBTI 정보가 담긴 DTO 리스트
     */
    public List<MbtiResponseDto> getAllMbtis() {
        return mbtiRepository.findAll()
                .stream()
                .map(mbti -> MbtiResponseDto.builder()
                        .id(mbti.getId())
                        .mbtiType(mbti.getMbtiType())
                        .mbtiName(mbti.getMbtiName())
                        .mbtiDesc(mbti.getMbtiDesc())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 MBTI 정보를 조회하는 메서드
     *
     * @param id MBTI ID
     * @return MBTI 정보 DTO
     * @throws ResponseStatusException MBTI를 찾을 수 없는 경우
     */
    public MbtiResponseDto getMbtiById(Long id) {
        return mbtiRepository.findById(id)
                .map(mbti -> MbtiResponseDto.builder()
                        .id(mbti.getId())
                        .mbtiType(mbti.getMbtiType())
                        .mbtiName(mbti.getMbtiName())
                        .mbtiDesc(mbti.getMbtiDesc())
                        .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MBTI not found"));
    }

    /**
     * MBTI 유형으로 MBTI 정보를 조회하는 메서드
     *
     * @param mbtiType MBTI 유형 문자열
     * @return MBTI 정보 DTO
     * @throws ResponseStatusException MBTI를 찾을 수 없는 경우
     */
    public MbtiResponseDto getMbtiByType(String mbtiType) {
        return mbtiRepository.findByMbtiType(mbtiType)
                .map(mbti -> MbtiResponseDto.builder()
                        .id(mbti.getId())
                        .mbtiType(mbti.getMbtiType())
                        .mbtiName(mbti.getMbtiName())
                        .mbtiDesc(mbti.getMbtiDesc())
                        .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MBTI type not found"));
    }
}