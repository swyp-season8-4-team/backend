package org.swyp.dessertbee.community.mate.service;

import org.swyp.dessertbee.community.mate.dto.request.MateApplyMemberRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateMemberResponse;

import java.util.List;
import java.util.UUID;

public interface MateMemberService {

    /** 디저트 메이트 생성 시 생성자 등록 */
    void addCreatorAsMember(UUID mateUuid, Long userId);

    /** 디저트메이트 삭제 시 멤버 삭제 */
    void deleteAllMember(Long mateId);

    /** 디저트 메이트 멤버 전체 조회 */
    List<MateMemberResponse> getMembers(UUID mateUuid);

    /** 디저트메이트 신청 api */
    void applyMate(UUID mateUuid);

    /** 디저트메이트 신청 취소 api */
    void cancelApplyMate(UUID mateUuid);

    /** 디저트 메이트 대기 멤버 전체 조회 */
    List<MateMemberResponse> pendingMate(UUID mateUuid);

    /** 디저트 메이트 멤버 신청 수락 api */
    void acceptMember (UUID mateUuid, MateApplyMemberRequest request);

    /** 디저트 메이트 멤버 신청 거절 api */
    void rejectMember (UUID mateUuid, MateApplyMemberRequest request);

    /** 디저트 메이트 멤버 강퇴 api */
    void bannedMember (UUID mateUuid, MateApplyMemberRequest request);

    /** 디저트 메이트 멤버 탈퇴 api */
    void leaveMember (UUID mateUuid);

}
