package org.swyp.dessertbee.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.swyp.dessertbee.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.entity.StoreTag;
import org.swyp.dessertbee.store.entity.TagCategory;
import org.swyp.dessertbee.store.repository.StoreRepository;
import org.swyp.dessertbee.store.repository.StoreTagRepository;
import org.swyp.dessertbee.store.repository.TagCategoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class StoreServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreTagRepository storeTagRepository;

    @Autowired
    private TagCategoryRepository tagCategoryRepository;

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
                .tagIds(List.of(tagId1, tagId2))
                .menus(List.of())
                .events(List.of())
                .coupons(List.of())
                .storeImages(List.of("img1.jpg", "img2.jpg")) // ✅ 파일명만 전달
                .menuImages(Map.of())
                .eventImages(Map.of())
                .build();
    }

    /** 가게 등록 테스트 */
    @Test
    void 가게_등록_API_테스트() throws Exception {
        // 1️⃣ JSON 변환
        String requestJson = objectMapper.writeValueAsString(request);

        // 2️⃣ API 호출 및 응답 검증
        mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.address").value(request.getAddress()))
                .andExpect(jsonPath("$.storeImages[0]").value(containsString("https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/img1.jpg")))
                .andExpect(jsonPath("$.storeImages[1]").value(containsString("https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/img2.jpg")));
    }

    /** 가게 상세 조회 API 테스트 */
    @Test
    void 가게_상세_조회_API_테스트() throws Exception {
        // 1️⃣ 가게 등록
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andReturn().getResponse().getContentAsString();

        // 2️⃣ 등록된 가게 ID 가져오기
        Long storeId = objectMapper.readTree(responseJson).get("id").asLong();

        // 3️⃣ 상세 조회 요청
        mockMvc.perform(get("/api/stores/" + storeId + "/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.address").value(request.getAddress()))
                .andExpect(jsonPath("$.storeImages[0]").value(containsString("https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/img1.jpg")))
                .andExpect(jsonPath("$.storeImages[1]").value(containsString("https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/img2.jpg")));
    }
}
