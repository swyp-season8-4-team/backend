package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.dto.request.UserBlockRequest;
import org.swyp.dessertbee.user.dto.response.UserBlockCheckResponse;
import org.swyp.dessertbee.user.dto.response.UserBlockResponse;
import org.swyp.dessertbee.user.entity.UserBlock;
import org.swyp.dessertbee.user.repository.UserBlockRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserBlockResponse blockUser(UUID blockerUuid, UserBlockRequest request) {
        // 차단하는 사용자 조회
        UserEntity blocker = userRepository.findByUserUuid(blockerUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 차단될 사용자 조회
        UserEntity blocked = userRepository.findByUserUuid(request.getBlockedUserUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신을 차단하려는 경우
        if (blocker.getId().equals(blocked.getId())) {
            throw new BusinessException(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }

        // 이미 차단한 사용자인지 확인
        if (userBlockRepository.existsByBlockerUserIdAndBlockedUserId(blocker.getId(), blocked.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_BLOCKED_USER);
        }

        // 차단 정보 저장
        UserBlock userBlock = UserBlock.builder()
                .blockerUserId(blocker.getId())
                .blockedUserId(blocked.getId())
                .build();

        UserBlock savedUserBlock = userBlockRepository.save(userBlock);
        log.info("사용자 차단 처리 완료: blocker={}, blocked={}", blocker.getNickname(), blocked.getNickname());

        return UserBlockResponse.builder()
                .id(savedUserBlock.getId())
                .blockerUserUuid(blocker.getUserUuid())
                .blockedUserUuid(blocked.getUserUuid())
                .blockedUserNickname(blocked.getNickname())
                .createdAt(savedUserBlock.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void unblockUser(UUID blockerUuid, Long blockId) {
        // 차단을 해제하려는 사용자 조회
        UserEntity blocker = userRepository.findByUserUuid(blockerUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 해당 차단 정보 조회
        UserBlock userBlock = userBlockRepository.findById(blockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_BLOCK_NOT_FOUND));

        // 차단을 해제하려는 사용자가 차단을 등록한 사용자인지 확인
        if (!userBlock.getBlockerUserId().equals(blocker.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "차단 해제 권한이 없습니다.");
        }

        userBlockRepository.delete(userBlock);
        log.info("사용자 차단 해제 완료: blockerId={}, blockId={}", blocker.getId(), blockId);
    }

    @Override
    public UserBlockResponse.ListResponse getBlockedUsers(UUID blockerUuid) {
        // 차단 목록을 조회하려는 사용자 조회
        UserEntity blocker = userRepository.findByUserUuid(blockerUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserBlock> userBlocks = userBlockRepository.findByBlockerUserId(blocker.getId());
        log.debug("사용자가 차단한 사용자 수: {}", userBlocks.size());

        List<UserBlockResponse> responses = userBlocks.stream().map(block -> {
            // 차단된 사용자 정보 조회
            UserEntity blocked = null;

            // 관계 매핑이 있는 경우 해당 관계를 활용
            if (block.getBlockedUser() != null) {
                blocked = block.getBlockedUser();
            } else {
                // 관계 매핑이 없는 경우 ID로 조회
                blocked = userRepository.findById(block.getBlockedUserId()).orElse(null);
            }

            String nickname = blocked != null ? blocked.getNickname() : "알 수 없음";
            UUID blockedUuid = blocked != null ? blocked.getUserUuid() : null;

            return UserBlockResponse.builder()
                    .id(block.getId())
                    .blockerUserUuid(blocker.getUserUuid())
                    .blockedUserUuid(blockedUuid)
                    .blockedUserNickname(nickname)
                    .createdAt(block.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return UserBlockResponse.ListResponse.builder()
                .blockedUsers(responses)
                .totalCount(responses.size())
                .build();
    }

    @Override
    public UserBlockCheckResponse checkBlockStatus(UUID blockerUuid, UUID blockedUuid) {
        Optional<UserBlock> userBlockOpt = userBlockRepository.findByBlockerUuidAndBlockedUuid(blockerUuid, blockedUuid);

        if (userBlockOpt.isPresent()) {
            UserBlock userBlock = userBlockOpt.get();

            // 차단된 사용자 정보 조회
            UserEntity blocked = null;

            // 관계 매핑이 있는 경우 해당 관계를 활용
            if (userBlock.getBlockedUser() != null) {
                blocked = userBlock.getBlockedUser();
            } else {
                // 관계 매핑이 없는 경우 ID로 조회
                blocked = userRepository.findById(userBlock.getBlockedUserId()).orElse(null);
            }

            String nickname = blocked != null ? blocked.getNickname() : "알 수 없음";

            return UserBlockCheckResponse.builder()
                    .isBlocked(true)
                    .id(userBlock.getId())
                    .blockerUserUuid(blockerUuid)
                    .blockedUserUuid(blockedUuid)
                    .blockedUserNickname(nickname)
                    .createdAt(userBlock.getCreatedAt())
                    .build();
        } else {
            return UserBlockCheckResponse.builder()
                    .isBlocked(false)
                    .blockerUserUuid(blockerUuid)
                    .blockedUserUuid(blockedUuid)
                    .build();
        }
    }

    @Override
    public List<Long> getBlockedUserIds(UUID blockerUuid) {
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlockerUuid(blockerUuid);
        log.debug("차단된 사용자 ID 목록 조회: count={}", blockedUserIds.size());
        return blockedUserIds;
    }

    @Override
    public List<UUID> getBlockedUserUuids(UUID blockerUuid) {
        List<UUID> blockedUserUuids = userBlockRepository.findBlockedUserUuidsByBlockerUuid(blockerUuid);
        log.debug("차단된 사용자 UUID 목록 조회: count={}", blockedUserUuids.size());
        return blockedUserUuids;
    }

    @Override
    public boolean isBlocked(UUID blockerUuid, UUID blockedUuid) {
        return userBlockRepository.existsByBlockerUuidAndBlockedUuid(blockerUuid, blockedUuid);
    }

    @Override
    @Transactional
    public UserBlockResponse blockUserById(Long blockerUserId, Long blockedUserId) {
        // 차단하는 사용자 조회
        UserEntity blocker = userRepository.findById(blockerUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 차단될 사용자 조회
        UserEntity blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 자기 자신을 차단하려는 경우
        if (blockerUserId.equals(blockedUserId)) {
            throw new BusinessException(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }

        // 이미 차단한 사용자인지 확인
        if (userBlockRepository.existsByBlockerUserIdAndBlockedUserId(blockerUserId, blockedUserId)) {
            throw new BusinessException(ErrorCode.ALREADY_BLOCKED_USER);
        }

        // 차단 정보 저장
        UserBlock userBlock = UserBlock.builder()
                .blockerUserId(blockerUserId)
                .blockedUserId(blockedUserId)
                .build();

        UserBlock savedUserBlock = userBlockRepository.save(userBlock);
        log.info("사용자 차단 처리 완료 (ID 기반): blocker={}, blocked={}", blockerUserId, blockedUserId);

        return UserBlockResponse.builder()
                .id(savedUserBlock.getId())
                .blockerUserUuid(blocker.getUserUuid())
                .blockedUserUuid(blocked.getUserUuid())
                .blockedUserNickname(blocked.getNickname())
                .createdAt(savedUserBlock.getCreatedAt())
                .build();
    }

    @Override
    public List<Long> getBlockedUserIdsByBlockerId(Long blockerUserId) {
        return userBlockRepository.findBlockedUserIdsByBlockerUserId(blockerUserId);
    }

    @Override
    public boolean isBlockedById(Long blockerUserId, Long blockedUserId) {
        return userBlockRepository.existsByBlockerUserIdAndBlockedUserId(blockerUserId, blockedUserId);
    }
}