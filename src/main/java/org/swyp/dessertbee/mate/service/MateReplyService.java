package org.swyp.dessertbee.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.mate.dto.MateUserIds;
import org.swyp.dessertbee.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.mate.entity.MateReply;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateReplyService {

    private final MateReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final MateReplyRepository mateReplyRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;

    /**
     * 디저트메이트 댓글 생성
     * */
    @Transactional
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

       UserEntity user = userRepository.findById(mateReply.getUserId())
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 유저입니다."));

        return MateReplyResponse.fromEntity(mateReply, mateUuid, user);
    }

    /**
     * 디저트메이트 댓글 전체 조회
     * */
    @Transactional
    public MateReplyPageResponse getReplies(UUID mateUuid, Pageable pageable) {
        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        // Pageable을 이용하여 데이터 조회
        Page<MateReply> repliesPage = mateReplyRepository.findAllByDeletedAtIsNull(mateId, pageable);

        // MateReplyResponse로 변환
        List<MateReplyResponse> repliesResponse = repliesPage.getContent()
                .stream()
                .map(mateReply -> getReplyDetail(mateUuid, mateReply.getMateReplyId()))
                .collect(Collectors.toList());

        // 다음 페이지 존재 여부
        boolean isLast = repliesPage.isLast();

        return new MateReplyPageResponse(repliesResponse, isLast);
    }


    /**
     * 디저트메이트 댓글 수정
     * */
    @Transactional
    public void updateReply(UUID mateUuid, Long replyId, MateReplyCreateRequest request) {

        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        //replyId 존재 여부 확인
        MateReply reply = mateReplyRepository.findByMateIdAndDeletedAtIsNull(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));


        reply.update(request.getContent());

    }


    /**
     * 디저트메이트 댓글 삭제
     * */
    @Transactional
    public void deleteReply(UUID mateUuid, Long replyId) {

        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        MateReply mateReply = mateReplyRepository.findByMateIdAndMateReplyId(mateId, replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        try {

            mateReply.softDelete();

            mateReplyRepository.save(mateReply);

        } catch (Exception e) {

            System.out.println("❌ 디저트메이트 멤버 탈퇴 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 댓글 삭제 실패: " + e.getMessage(), e);
        }
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
