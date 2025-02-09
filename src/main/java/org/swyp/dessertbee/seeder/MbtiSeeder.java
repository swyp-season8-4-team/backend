package org.swyp.dessertbee.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.user.entity.MbtiEntity;
import org.swyp.dessertbee.user.repository.MbtiRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MbtiSeeder implements CommandLineRunner {

    private final MbtiRepository mbtiRepository;

    @Override
    public void run(String... args) {
        List<MbtiEntity> mbtiTypes = List.of(
                // 외향적인 벌 (E)
                MbtiEntity.builder()
                        .type("ENFP")
                        .name("에너지벌")
                        .desc("신상과 트렌드를 따라가는 도전적인 디저트러버!")
                        .build(),
                MbtiEntity.builder()
                        .type("ENTP")
                        .name("번쩍벌")
                        .desc("독특한 조합과 색다른 디저트를 탐험하는 크리에이터!")
                        .build(),
                MbtiEntity.builder()
                        .type("ESFP")
                        .name("트윙클벌")
                        .desc("화려한 비주얼, 인스타 감성 디저트를 사랑하는 감각적인 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ENTJ")
                        .name("리더벌")
                        .desc("완벽한 퀄리티와 프리미엄 디저트를 찾는 전략가 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ESFJ")
                        .name("하모니벌")
                        .desc("모두가 좋아할 디저트를 찾는 분위기 메이커 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ESTP")
                        .name("파워벌")
                        .desc("강렬한 맛, 리미티드 에디션 디저트를 시도하는 모험가 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ENFJ")
                        .name("화이팅벌")
                        .desc("함께 나눌 수 있는 디저트를 선호하는 팀플레이어 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("INTJ")
                        .name("길잡이벌")
                        .desc("체계적이고 전략적으로 디저트를 선택하는 분석가 벌!")
                        .build(),
                // 내향적인 벌 (I)
                MbtiEntity.builder()
                        .type("INFJ")
                        .name("드림벌")
                        .desc("감성적이고 특별한 스토리가 담긴 디저트를 선호하는 몽상가 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ISFJ")
                        .name("포근벌")
                        .desc("따뜻한 분위기에서 클래식한 디저트를 즐기는 안정적인 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ISFP")
                        .name("감성벌")
                        .desc("섬세한 비주얼과 정교한 디저트를 사랑하는 아티스트 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ISTP")
                        .name("솔로몬벌")
                        .desc("기본에 충실한 맛을 선호하는 현실적이고 논리적인 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("ISTJ")
                        .name("청렴벌")
                        .desc("신뢰할 수 있는 검증된 디저트를 선택하는 원칙적인 벌!")
                        .build(),
                MbtiEntity.builder()
                        .type("INFP")
                        .name("만약에벌")
                        .desc("새로운 디저트 조합을 꿈꾸는 상상력 넘치는 벌!")
                        .build()
        );

        for (MbtiEntity mbti : mbtiTypes) {
            if (mbtiRepository.findByType(mbti.getType()).isEmpty()) {
                mbtiRepository.save(mbti);
                System.out.println("Seeded MBTI: " + mbti.getType() + " - " + mbti.getName());
            }
        }
    }
}