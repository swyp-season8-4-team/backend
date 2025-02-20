package org.swyp.dessertbee.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.mate.dto.MateUserIds;
import org.swyp.dessertbee.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateReply;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MateReplyService {

    private final MateReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final MateReplyRepository mateReplyRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;

    /**
     * 디저트메이트 댓글 생성
     * */
    public MateReplyResponse createReply(UUID mateUuid, MateReplyCreateRequest request) {

        //디저트 메이트 유효성 검사
        MateUserIds mateUserIds = validateMateAndUser(mateUuid, request.getUserUuid());
        Long mateId = mateUserIds.getMateId();
        Long userId = mateUserIds.getUserId();


        MateReply mateReply = replyRepository.save(
                MateReply.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .content(request.getContent())
                        .report(null)
                .build()
        );

        return getReplyDetail(mateUuid, mateReply.getMateReplyId());
    }

    /**
     * 디저트메이트 댓글 조회(한개만)
     * */
    public MateReplyResponse getReplyDetail(UUID mateUuid, Long replyId) {

        //디저트 메이트 유효성 검사
        validateMate(mateUuid);


        MateReply mateReply = mateReplyRepository.findById(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        UUID userUuid = userRepository.findUserUuidById(mateReply.getUserId());


        return MateReplyResponse.fromEntity(mateReply, mateUuid, userUuid);
    }

    /**
     * 디저트메이트 댓글 전체 조회
     * */
    public MateReplyPageResponse getReplies(UUID mateUuid, int from, int to) {

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int limit = to - from;

        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        // limit + 1 만큼 데이터를 가져와서 다음 데이터가 있는지 확인
        List<MateReply> replies = mateReplyRepository.findAllByDeletedAtIsNull(mateId, from, limit + 1);

        List<MateReplyResponse> repliesResponse = mateReplyRepository.findAllByDeletedAtIsNull(mateId, from, limit)
                .stream()
                .map( mateReply -> {
                   return getReplyDetail(mateUuid, mateReply.getMateReplyId());
                })
                .collect(Collectors.toList());

        // limit보다 적은 개수가 조회되면 마지막 데이터임
        boolean isLast = replies.size() <= limit;


        return new MateReplyPageResponse(repliesResponse, isLast);
    }


    /**
     * 디저트메이트 댓글 수정
     * */
    public void updateReply(UUID mateUuid, Long replyId, MateReplyCreateRequest request) {

        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        //replyId 존재 여부 확인
        MateReply reply = mateReplyRepository.findByMateIdAndDeletedAtIsNull(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));


        reply.update(request.getContent());

    }

    /**
     * Mate와 User 한번에 유효성 검사
     * */
    private MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {

        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        //디저트 메이트 멤버인지 확인
        mateMemberRepository.findByMateIdAndUserId(mateId, userId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));


        return new MateUserIds(mateId, userId);
    }

    /**
     * Mate만 유효성 검사
     * */
    public MateUserIds validateMate (UUID mateUuid){


        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);


        mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        return new MateUserIds(mateId, null);
    }
}
