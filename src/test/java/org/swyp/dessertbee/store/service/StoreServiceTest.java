package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.swyp.dessertbee.store.dto.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.StoreResponse;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.repository.StoreRepository;
import org.swyp.dessertbee.store.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.repository.StoreTagRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class StoreServiceTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreTagRepository storeTagRepository;

    @Autowired
    private StoreTagRelationRepository storeTagRelationRepository;

    @BeforeEach
    void setUp() {
        storeRepository.deleteAll();
        storeTagRepository.deleteAll();
        storeTagRelationRepository.deleteAll();
    }

    @Test
    @DisplayName("가게 등록 테스트 - 태그 포함")
    void createStore_withTags_shouldSaveCorrectly() {
        // Given (테스트 데이터 준비)
        StoreCreateRequest request = new StoreCreateRequest(
                1L,  // ownerId
                "감성커피",  // name
                "010-1234-5678",
                "서울특별시 강남구",
                "https://instagram.com/test",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                "아늑한 카페입니다.",
                true,  // animalYn
                false, // tumblerYn
                true,  // parkingYn
                "09:00 - 22:00",
                "일요일",
                null,  // status 기본값 (APPROVED)
                List.of("커피 맛집", "디저트 맛집", "아늑함") // ✅ 태그 포함
        );

        // When (가게 등록 실행)
        StoreResponse response = storeService.createStore(request);

        // Then (검증)
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("감성커피");

        // DB에서 저장된 가게 확인
        Store savedStore = storeRepository.findById(response.id()).orElseThrow();
        assertThat(savedStore.getName()).isEqualTo("감성커피");
        assertThat(savedStore.getAddress()).isEqualTo("서울특별시 강남구");
        assertThat(savedStore.getAnimalYn()).isTrue();
        assertThat(savedStore.getParkingYn()).isTrue();
        assertThat(savedStore.getTumblerYn()).isFalse();

        // 태그가 정상적으로 저장되었는지 확인
        List<String> savedTags = storeTagRelationRepository.findByStore(savedStore).stream()
                .map(relation -> relation.getTag().getName())
                .toList();

        assertThat(savedTags).containsExactlyInAnyOrder("커피 맛집", "디저트 맛집", "아늑함"); // 태그 중복 없이 저장됨
    }
}
