package org.swyp.dessertbee.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.swyp.dessertbee.store.dto.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.StoreResponse;
import org.swyp.dessertbee.store.entity.StoreStatus;
import org.swyp.dessertbee.store.service.StoreService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @Test
    @WithMockUser
    @DisplayName("가게 등록 API 테스트")
    void createStore_shouldReturnCreatedStore() throws Exception {
        // Given
        StoreCreateRequest request = new StoreCreateRequest(
                1L,
                "감성커피",
                "010-1234-5678",
                "서울특별시 강남구",
                "https://instagram.com/test",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                "아늑한 카페입니다.",
                true,
                false,
                true,
                "09:00 - 22:00",
                "일요일",
                StoreStatus.APPROVED,
                List.of("커피 맛집", "디저트 맛집", "아늑함")
        );

        StoreResponse response = new StoreResponse(
                1L,
                1L,  // ✅ ownerId 추가
                "감성커피",
                "010-1234-5678",
                "서울특별시 강남구",
                "https://instagram.com/test",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                "아늑한 카페입니다.",
                true,
                false,
                true,
                "09:00 - 22:00",
                "일요일",
                new BigDecimal("0.0"), // ✅ averageRating 추가
                StoreStatus.APPROVED,
                LocalDateTime.now(), // ✅ createdAt 추가
                LocalDateTime.now(), // ✅ updatedAt 추가
                List.of("커피 맛집", "디저트 맛집", "아늑함")
        );

        when(storeService.createStore(any(StoreCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerId", is(1))) // ✅ ownerId 확인
                .andExpect(jsonPath("$.name", is("감성커피")))
                .andExpect(jsonPath("$.address", is("서울특별시 강남구")))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.tags[0]", is("커피 맛집")))
                .andExpect(jsonPath("$.tags[1]", is("디저트 맛집")))
                .andExpect(jsonPath("$.tags[2]", is("아늑함")));
    }
}
