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

    @Transactional
    public void seedTagCategories() {
        List<String> categoryNames = List.of(
                "베이커리", "디저트", "전통 간식", "스페셜/시즌 디저트"
        );

        for (String name : categoryNames) {
            tagCategoryRepository.findByName(name)
                    .orElseGet(() -> {
                        TagCategory category = TagCategory.builder().name(name).build();
                        return tagCategoryRepository.save(category);
                    });
        }
    }

    @Transactional
    public void seedStoreTags() {
        List<StoreTag> tags = List.of(
                // 베이커리
                new StoreTag(null, "베이글", findCategory("베이커리")),
                new StoreTag(null, "샌드위치 / 핫도그", findCategory("베이커리")),
                new StoreTag(null, "프레첼", findCategory("베이커리")),
                new StoreTag(null, "도넛", findCategory("베이커리")),
                new StoreTag(null, "소금빵", findCategory("베이커리")),
                new StoreTag(null, "타르트", findCategory("베이커리")),
                new StoreTag(null, "토스트", findCategory("베이커리")),
                new StoreTag(null, "크루아상", findCategory("베이커리")),
                new StoreTag(null, "파이", findCategory("베이커리")),
                new StoreTag(null, "식사빵", findCategory("베이커리")),

                // 디저트
                new StoreTag(null, "케이크", findCategory("디저트")),
                new StoreTag(null, "구움과자", findCategory("디저트")),
                new StoreTag(null, "마카롱", findCategory("디저트")),
                new StoreTag(null, "브라우니", findCategory("디저트")),
                new StoreTag(null, "와플", findCategory("디저트")),
                new StoreTag(null, "브런치", findCategory("디저트")),
                new StoreTag(null, "초콜릿", findCategory("디저트")),
                new StoreTag(null, "푸딩", findCategory("디저트")),
                new StoreTag(null, "젤리", findCategory("디저트")),
                new StoreTag(null, "사탕", findCategory("디저트")),

                // 전통 간식
                new StoreTag(null, "붕어빵", findCategory("전통 간식")),
                new StoreTag(null, "꽈배기", findCategory("전통 간식")),
                new StoreTag(null, "호두과자", findCategory("전통 간식")),
                new StoreTag(null, "호떡", findCategory("전통 간식")),
                new StoreTag(null, "떡", findCategory("전통 간식")),
                new StoreTag(null, "약과", findCategory("전통 간식")),
                new StoreTag(null, "오란다", findCategory("전통 간식")),
                new StoreTag(null, "한과", findCategory("전통 간식")),
                new StoreTag(null, "개성주악", findCategory("전통 간식")),
                new StoreTag(null, "정과", findCategory("전통 간식")),

                // 스페셜/시즌 디저트
                new StoreTag(null, "그릭요거트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "아이스크림", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "빙수", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "파르페", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "퓨전 디저트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "시즌 디저트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "건강 디저트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "월드 디저트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "커스텀 디저트", findCategory("스페셜/시즌 디저트")),
                new StoreTag(null, "지역 특산 디저트", findCategory("스페셜/시즌 디저트"))
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
