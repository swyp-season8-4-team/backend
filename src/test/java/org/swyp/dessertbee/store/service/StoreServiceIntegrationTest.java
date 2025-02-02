package org.swyp.dessertbee.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    private MockMultipartFile file1;
    private MockMultipartFile file2;
    private final String s3BaseUrl = "https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/";

    @BeforeEach
    void setUp() {
        // ✅ 1️⃣ 태그 카테고리 확인 후 저장
        Optional<TagCategory> categoryOptional = tagCategoryRepository.findByName("Food");
        TagCategory category = categoryOptional.orElseGet(() -> tagCategoryRepository.save(
                TagCategory.builder().name("Food").build()
        ));

        // ✅ 2️⃣ StoreTag 확인 후 저장
        Optional<StoreTag> tag1 = storeTagRepository.findByName("Dessert");
        Optional<StoreTag> tag2 = storeTagRepository.findByName("Cafe");

        if (tag1.isEmpty() || tag2.isEmpty()) {
            storeTagRepository.saveAll(List.of(
                    StoreTag.builder().name("Dessert").category(category).build(),
                    StoreTag.builder().name("Cafe").category(category).build()
            ));
        }

        // ✅ 3️⃣ 최신 태그 ID 가져오기
        Long tagId1 = storeTagRepository.findByName("Dessert").orElseThrow().getId();
        Long tagId2 = storeTagRepository.findByName("Cafe").orElseThrow().getId();

        // ✅ 4️⃣ MockMultipartFile 생성
        file1 = new MockMultipartFile("storeImageFiles", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image-1".getBytes());
        file2 = new MockMultipartFile("storeImageFiles", "img2.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image-2".getBytes());
    }

    @Test
    void 가게_등록_API_테스트() throws Exception {
        String requestJson = objectMapper.writeValueAsString(
                StoreCreateRequest.builder()
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
                        .tagIds(List.of(1L, 2L))
                        .menus(List.of())
                        .events(List.of())
                        .coupons(List.of())
                        .build()
        );

        MockMultipartFile jsonRequest = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, requestJson.getBytes()
        );

        // ✅ API 호출 및 응답 검증
        String responseJson = mockMvc.perform(multipart("/api/stores")
                        .file(file1)
                        .file(file2)
                        .file(jsonRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Store"))
                .andExpect(jsonPath("$.address").value("Seoul, Korea"))
                .andReturn().getResponse().getContentAsString();

        // ✅ 가게 ID 추출
        Long storeId = objectMapper.readTree(responseJson).get("id").asLong();

        // ✅ 상세 조회 요청 및 검증
        String detailsResponseJson = mockMvc.perform(get("/api/stores/" + storeId + "/details"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // ✅ 실제 응답에서 이미지 URL 가져오기
        JsonNode responseNode = objectMapper.readTree(detailsResponseJson);
        String actualImageUrl1 = responseNode.get("storeImages").get(0).asText();
        String actualImageUrl2 = responseNode.get("storeImages").get(1).asText();

        // ✅ 예상된 UUID 포함된 이미지 URL 형식 검증
        assertThat(actualImageUrl1).matches("^" + s3BaseUrl + "store/" + storeId + "/[a-f0-9\\-]+-img1\\.jpg$");
        assertThat(actualImageUrl2).matches("^" + s3BaseUrl + "store/" + storeId + "/[a-f0-9\\-]+-img2\\.jpg$");
    }

    @Test
    void 가게_상세_조회_API_테스트() throws Exception {
        // ✅ 먼저 가게 등록 API 호출
        String createResponseJson = mockMvc.perform(multipart("/api/stores")
                        .file(file1)
                        .file(file2)
                        .file(new MockMultipartFile(
                                "request", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(
                                        StoreCreateRequest.builder()
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
                                                .tagIds(List.of(1L, 2L))
                                                .menus(List.of())
                                                .events(List.of())
                                                .coupons(List.of())
                                                .build()
                                ).getBytes()
                        ))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // ✅ 생성된 storeId 가져오기
        Long storeId = objectMapper.readTree(createResponseJson).get("id").asLong();

        // ✅ 가게 상세 조회 API 호출 및 검증
        String responseJson = mockMvc.perform(get("/api/stores/" + storeId + "/details"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // ✅ JSON 파싱하여 storeImages 검증
        JsonNode responseNode = objectMapper.readTree(responseJson);
        String actualImageUrl1 = responseNode.get("storeImages").get(0).asText();
        String actualImageUrl2 = responseNode.get("storeImages").get(1).asText();

        // ✅ 예상된 UUID 포함된 이미지 URL 형식 검증
        assertThat(actualImageUrl1).matches("^" + s3BaseUrl + "store/" + storeId + "/[a-f0-9\\-]+-img1\\.jpg$");
        assertThat(actualImageUrl2).matches("^" + s3BaseUrl + "store/" + storeId + "/[a-f0-9\\-]+-img2\\.jpg$");
    }
}
