package org.swyp.dessertbee.seeder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.store.store.entity.StoreTag;
import org.swyp.dessertbee.store.store.entity.TagCategory;
import org.swyp.dessertbee.store.store.repository.StoreTagRepository;
import org.swyp.dessertbee.store.store.repository.TagCategoryRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreTagSeeder implements CommandLineRunner {

    private final TagCategoryRepository tagCategoryRepository;
    private final StoreTagRepository storeTagRepository;

    @Override
    public void run(String... args) {
        seedTagCategories();
        seedStoreTags();
    }

    @Transactional  // 트랜잭션을 별도로 분리하여 적용
    public void seedTagCategories() {
        List<String> categoryNames = List.of(
                "음식 종류", "음료 종류", "편의 시설", "분위기", "운영 스타일", "특화 서비스"
        );

        for (String name : categoryNames) {
            tagCategoryRepository.findByName(name)
                    .orElseGet(() -> {
                        TagCategory category = TagCategory.builder().name(name).build();
                        return tagCategoryRepository.save(category);
                    });
        }
    }

    @Transactional  // 트랜잭션을 별도로 분리하여 적용
    public void seedStoreTags() {
        List<StoreTag> tags = List.of(
                // 음식 종류
                new StoreTag(null, "디저트 전문점", findCategory("음식 종류")),
                new StoreTag(null, "베이커리", findCategory("음식 종류")),
                new StoreTag(null, "브런치 제공", findCategory("음식 종류")),
                new StoreTag(null, "비건 옵션 있음", findCategory("음식 종류")),

                // 음료 종류
                new StoreTag(null, "스페셜티 커피", findCategory("음료 종류")),
                new StoreTag(null, "차(Tea) 전문", findCategory("음료 종류")),
                new StoreTag(null, "수제 음료", findCategory("음료 종류")),
                new StoreTag(null, "와인 및 칵테일 제공", findCategory("음료 종류")),

                // 편의 시설
                new StoreTag(null, "주차 가능", findCategory("편의 시설")),
                new StoreTag(null, "콘센트 제공", findCategory("편의 시설")),
                new StoreTag(null, "와이파이 가능", findCategory("편의 시설")),
                new StoreTag(null, "넓은 좌석", findCategory("편의 시설")),
                new StoreTag(null, "테라스 좌석 있음", findCategory("편의 시설")),

                // 분위기
                new StoreTag(null, "조용한 분위기", findCategory("분위기")),
                new StoreTag(null, "활기찬 분위기", findCategory("분위기")),
                new StoreTag(null, "루프탑 있음", findCategory("분위기")),
                new StoreTag(null, "감성적인 인테리어", findCategory("분위기")),

                // 운영 스타일
                new StoreTag(null, "로컬 카페", findCategory("운영 스타일")),
                new StoreTag(null, "프랜차이즈 카페", findCategory("운영 스타일")),
                new StoreTag(null, "무인 운영", findCategory("운영 스타일")),

                // 특화 서비스
                new StoreTag(null, "애완동물 동반 가능", findCategory("특화 서비스")),
                new StoreTag(null, "도서 구비", findCategory("특화 서비스")),
                new StoreTag(null, "개인 작업하기 좋음", findCategory("특화 서비스")),
                new StoreTag(null, "예약 가능", findCategory("특화 서비스")),
                new StoreTag(null, "배달 및 테이크아웃 가능", findCategory("특화 서비스"))
        );

        for (StoreTag tag : tags) {
            storeTagRepository.findByName(tag.getName())
                    .orElseGet(() -> storeTagRepository.save(tag));
        }
    }

    private TagCategory findCategory(String categoryName) {
        return tagCategoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("태그 카테고리를 찾을 수 없습니다: " + categoryName));
    }
}
