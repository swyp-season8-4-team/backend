package org.swyp.dessertbee.store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.swyp.dessertbee.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.entity.StoreTag;
import org.swyp.dessertbee.store.entity.TagCategory;
import org.swyp.dessertbee.store.repository.StoreRepository;
import org.swyp.dessertbee.store.repository.StoreStatisticsRepository;
import org.swyp.dessertbee.store.repository.StoreTagRepository;
import org.swyp.dessertbee.store.repository.TagCategoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")  // 테스트 환경 설정 적용
class StoreServiceIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreTagRepository storeTagRepository;

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

    @Autowired
    private StoreStatisticsRepository storeStatisticsRepository;

    private StoreCreateRequest request;

    @BeforeEach
    void setUp() {
        // 1️⃣ 태그 카테고리 존재 여부 확인 후 저장
        Optional<TagCategory> categoryOptional = tagCategoryRepository.findByName("Food");
        TagCategory category = categoryOptional.orElseGet(() -> tagCategoryRepository.save(
                TagCategory.builder().name("Food").build()
        ));

        // 2️⃣ StoreTag 존재 여부 확인 후 저장
        Optional<StoreTag> tag1 = storeTagRepository.findByName("Dessert");
        Optional<StoreTag> tag2 = storeTagRepository.findByName("Cafe");

        if (tag1.isEmpty() || tag2.isEmpty()) {
            storeTagRepository.saveAll(List.of(
                    StoreTag.builder().name("Dessert").category(category).build(),
                    StoreTag.builder().name("Cafe").category(category).build()
            ));
        }

        // 3️⃣ 최신 태그 ID 가져오기
        Long tagId1 = storeTagRepository.findByName("Dessert").orElseThrow().getId();
        Long tagId2 = storeTagRepository.findByName("Cafe").orElseThrow().getId();
        request = StoreCreateRequest.builder()
                .ownerId(100L)
                .name("Integration Test Store")
                .phone("010-5678-1234")
                .address("Seoul, Korea")
                .storeLink("http://test.com")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .description("Test description")
                .animalYn(true)
                .tumblerYn(true)
                .parkingYn(true)
                .operatingHours("09:00 - 22:00")
                .closingDays("Sunday")
                .tagIds(List.of(1L, 2L)) // ✅ `List<Long>`으로 설정
                .menus(List.of()) // ✅ `List.of()` 사용하여 `null` 방지
                .events(List.of())
                .coupons(List.of())
                .storeImages(List.of("img1.jpg", "img2.jpg"))
                .menuImages(Map.of()) // ✅ `null` 대신 빈 `Map` 사용
                .eventImages(Map.of())
                .build();
    }

    @Test
    void 가게_등록_및_조회_통합_테스트() {
        // 1️⃣ 가게 등록
        StoreDetailResponse response = storeService.createStore(request);

        // 2️⃣ 등록된 가게 DB에서 조회
        Store savedStore = storeRepository.findById(response.getId()).orElse(null);

        // 3️⃣ 검증
        assertThat(savedStore).isNotNull();
        assertThat(savedStore.getName()).isEqualTo(request.getName());
        assertThat(savedStore.getAddress()).isEqualTo(request.getAddress());

        // 4️⃣ 통계 정보 확인
        assertThat(storeStatisticsRepository.findByStoreId(savedStore.getId())).isNotNull();
    }

    @Test
    void 가게_조회_통합_테스트() {
        // 1️⃣ 가게 등록
        StoreDetailResponse createdStore = storeService.createStore(request);

        // 2️⃣ 가게 상세 정보 조회
        StoreDetailResponse response = storeService.getStoreDetails(createdStore.getId());

        // 3️⃣ 검증
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getAddress()).isEqualTo(request.getAddress());
        assertThat(response.getStoreImages()).isNotEmpty(); // 대표 이미지가 저장되었는지 확인
    }
}
