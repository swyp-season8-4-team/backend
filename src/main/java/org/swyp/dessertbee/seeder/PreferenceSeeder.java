package org.swyp.dessertbee.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PreferenceSeeder implements CommandLineRunner {

    private final PreferenceRepository preferenceRepository;

    @Override
    public void run(String... args) {
        List<PreferenceEntity> preferences = List.of(
                // 건강 & 식단 관련
                PreferenceEntity.builder()
                        .preferenceName("비건")
                        .preferenceDesc("동물성 재료 없이!")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("글루텐프리")
                        .preferenceDesc("밀가루 없는 디저트")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("락토프리")
                        .preferenceDesc("유당 없는 디저트")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("로우슈가")
                        .preferenceDesc("당 줄인 건강한 디저트")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("키토제닉")
                        .preferenceDesc("저탄고지 디저트")
                        .build(),

                // 트렌드 & 감성 관련
                PreferenceEntity.builder()
                        .preferenceName("할매픽")
                        .preferenceDesc("전통 + 감성 디저트")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("트렌디")
                        .preferenceDesc("SNS 인기 디저트")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("비주얼")
                        .preferenceDesc("예쁜 디저트")
                        .build(),

                // 경험 & 독창성 관련
                PreferenceEntity.builder()
                        .preferenceName("리미티드")
                        .preferenceDesc("한정판/시즌 한정 메뉴")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("로컬라이징")
                        .preferenceDesc("지역 특색 살린 메뉴")
                        .build(),
                PreferenceEntity.builder()
                        .preferenceName("꿀조합")
                        .preferenceDesc("맛 조합이 환상적인 디저트")
                        .build()
        );

        for (PreferenceEntity preference : preferences) {
            if (preferenceRepository.findByPreferenceName(preference.getPreferenceName()).isEmpty()) {
                preferenceRepository.save(preference);
                System.out.println("Seeded Preference: " + preference.getPreferenceName());
            }
        }
    }
}