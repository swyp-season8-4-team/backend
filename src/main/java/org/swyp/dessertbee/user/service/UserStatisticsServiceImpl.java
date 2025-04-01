package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserRepository userRepository;

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public List<UserStatisticsResponseDto> getAllUsers() {
        return userRepository.findAllUsersWithRoles();
    }
}
