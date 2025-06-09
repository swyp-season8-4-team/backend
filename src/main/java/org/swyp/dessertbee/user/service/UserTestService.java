package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.saved.repository.UserStoreListRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.repository.UserBlockRepository;

/**
 * 사용자 테스트용 서비스
 * 릴리즈 환경에서만 활성화되며, 사용자와 관련된 모든 데이터를 완전히 삭제하는 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserTestService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserService userService;
    private final UserStoreListRepository userStoreListRepository;

    /**
     * 현재 로그인한 사용자와 관련된 모든 데이터를 완전히 삭제합니다.
     * JWT 토큰을 통해 현재 사용자를 식별합니다.
     *
     * @return 삭제된 사용자의 이메일
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public String hardDeleteCurrentUserWithAllRelatedData() {
        log.warn("현재 로그인 사용자 Hard Delete 시작");

        // JWT 토큰에서 현재 사용자 조회
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND,
                    "현재 로그인한 사용자를 찾을 수 없습니다.");
        }

        String email = currentUser.getEmail();
        performHardDeleteWithAllRelatedData(currentUser);

        log.warn("현재 로그인 사용자 Hard Delete 완료 - email: {}", email);
        return email;
    }

    /**
     * 사용자와 관련된 모든 데이터를 순서대로 완전 삭제합니다.
     *
     * @param user 삭제할 사용자
     */
    private void performHardDeleteWithAllRelatedData(UserEntity user) {
        Long userId = user.getId();
        String email = user.getEmail();

        try {
            log.info("관련 데이터 정리 시작 - userId: {}, email: {}", userId, email);

            // 사용자 차단 관련 데이터 삭제 (양방향 매핑이 아니라서 따로 삭제)
            cleanupUserData(userId);

            // 사용자 삭제 (CASCADE로 연관 데이터 자동 삭제)
            userRepository.delete(user);

            log.info("사용자 완전 삭제 완료 - userId: {}, email: {}", userId, email);

        } catch (Exception e) {
            log.error("사용자 완전 삭제 실패 - userId: {}, email: {}", userId, email, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "사용자 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 차단 관련 데이터 정리
     */
    private void cleanupUserData(Long userId) {
        try {
            int deletedCount = userBlockRepository.deleteByUserId(userId);
            deletedCount = deletedCount + userStoreListRepository.deleteByUserId(userId);
            log.info("사용자 차단 데이터 정리 완료 - userId: {}, 삭제된 레코드: {} 개", userId, deletedCount);
        } catch (Exception e) {
            log.error("사용자 차단 데이터 정리 실패 - userId: {}", userId, e);
            throw e;
        }
    }
}