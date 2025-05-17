package org.swyp.dessertbee.user.service;

import org.swyp.dessertbee.user.dto.request.UserBlockRequest;
import org.swyp.dessertbee.user.dto.response.UserBlockCheckResponse;
import org.swyp.dessertbee.user.dto.response.UserBlockResponse;

import java.util.List;
import java.util.UUID;

public interface UserBlockService {

    /**
     * 사용자 차단하기
     * @param blockerUuid 차단하는 사용자 UUID
     * @param request 차단 요청 DTO (차단될 사용자 UUID 포함)
     * @return 차단 정보 응답 DTO
     */
    UserBlockResponse blockUser(UUID blockerUuid, UserBlockRequest request);

    /**
     * 사용자 차단 해제하기
     * @param blockerUuid 차단 해제하는 사용자 UUID
     * @param blockId 차단 ID
     */
    void unblockUser(UUID blockerUuid, Long blockId);

    /**
     * 차단한 사용자 목록 조회
     * @param blockerUuid 조회하는 사용자 UUID
     * @return 차단 목록 응답 DTO
     */
    UserBlockResponse.ListResponse getBlockedUsers(UUID blockerUuid);

    /**
     * 차단 여부와 상세 정보 확인
     * @param blockerUuid 차단한 사용자 UUID
     * @param blockedUuid 차단된 사용자 UUID
     * @return 차단 여부 및 상세 정보 응답 DTO
     */
    UserBlockCheckResponse checkBlockStatus(UUID blockerUuid, UUID blockedUuid);

    /**
     * 특정 사용자가 차단한 사용자 ID 목록 조회
     * @param blockerUuid 조회하는 사용자 UUID
     * @return 차단한 사용자 ID 목록
     */
    List<Long> getBlockedUserIds(UUID blockerUuid);

    /**
     * 특정 사용자가 차단한 사용자 UUID 목록 조회
     * @param blockerUuid 조회하는 사용자 UUID
     * @return 차단한 사용자 UUID 목록
     */
    List<UUID> getBlockedUserUuids(UUID blockerUuid);

    /**
     * 차단 여부 확인
     * @param blockerUuid 차단한 사용자 UUID
     * @param blockedUuid 차단된 사용자 UUID
     * @return 차단 여부
     */
    boolean isBlocked(UUID blockerUuid, UUID blockedUuid);

    /**
     * ID 기반: 사용자 차단하기 (내부용)
     * @param blockerUserId 차단하는 사용자 ID
     * @param blockedUserId 차단되는 사용자 ID
     * @return 차단 정보 응답 DTO
     */
    UserBlockResponse blockUserById(Long blockerUserId, Long blockedUserId);

    /**
     * ID 기반: 특정 사용자가 차단한 사용자 ID 목록 조회 (내부용)
     * @param blockerUserId 조회하는 사용자 ID
     * @return 차단한 사용자 ID 목록
     */
    List<Long> getBlockedUserIdsByBlockerId(Long blockerUserId);

    /**
     * ID 기반: 차단 여부 확인 (내부용)
     * @param blockerUserId 차단한 사용자 ID
     * @param blockedUserId 차단된 사용자 ID
     * @return 차단 여부
     */
    boolean isBlockedById(Long blockerUserId, Long blockedUserId);
}