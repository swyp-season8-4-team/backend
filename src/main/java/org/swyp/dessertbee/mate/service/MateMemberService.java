package org.swyp.dessertbee.mate.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MateMemberService {


    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;
    private MateMember mateMember;
    /**
     * 디저트 메이트 생성 시 생성자 등록
     * */
    public void addCreatorAsMember(UUID mateUuid, Long userId) {

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);
        //mateMember 테이블에 생성자 등록
        mateMemberRepository.save(
                MateMember.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .grade(MateMemberGrade.CREATOR)
                        .approvalYn(true)
                        .build()
        );
    }
    /**
     * 디저트메이트 삭제 시 멤버 삭제
     * */
    public void deleteAllMember(UUID mateUuid) {

        //mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        try {


            // mateId로 모든 멤버 조회
            List<MateMember> members = mateMemberRepository.findAllByMateId(mateId);

            // 각 멤버에 대해 softDelete 처리
            for (MateMember member : members) {
                member.softDelete();  // 개별 멤버에 softDelete 적용
            }

            // 변경된 모든 멤버 저장
            mateMemberRepository.saveAll(members);



        }catch (Exception e){
            System.out.println("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
        }

    }


//    public List<MateMemberResponse> getMemberList(UUID mateUuid) {
//
//        //프론트에서 받아온 mateUuid로 mateId 조회
//        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);
//
//
//         return mateMemberRepository.findMateMemberByMateId()
//    }
}
