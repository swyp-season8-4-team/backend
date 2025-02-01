package org.swyp.dessertbee.store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.dto.request.*;
import org.swyp.dessertbee.store.dto.response.*;
import org.swyp.dessertbee.store.entity.*;
import org.swyp.dessertbee.store.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreTagRepository storeTagRepository;
    @Mock
    private StoreTagRelationRepository storeTagRelationRepository;
    @Mock
    private StoreReviewRepository storeReviewRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private StoreStatisticsRepository storeStatisticsRepository;
    @Mock
    private ImageService imageService;

    private Store sampleStore;
    private StoreTag sampleTag1;
    private StoreTag sampleTag2;

    @BeforeEach
    void setUp() {
        sampleStore = Store.builder()
                .id(1L)
                .ownerId(100L)
                .name("Test Store")
                .phone("010-1234-5678")
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
                .averageRating(BigDecimal.valueOf(4.5))
                .status(StoreStatus.APPROVED)
                .build();

        sampleTag1 = StoreTag.builder().id(1L).name("Dessert").build();
        sampleTag2 = StoreTag.builder().id(2L).name("Cafe").build();
    }

    /** 가게 등록 테스트 */
    @Test
    void createStore_Success() {
        // Given
        List<Long> tagIds = List.of(1L, 2L);

        StoreCreateRequest request = StoreCreateRequest.builder()
                .ownerId(100L)
                .name("Test Store")
                .phone("010-1234-5678")
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
                .storeImages(List.of("img1.jpg", "img2.jpg"))
                .menuImages(Map.of())
                .eventImages(Map.of())
                .build();

        when(storeRepository.save(any(Store.class))).thenReturn(sampleStore);
        when(storeStatisticsRepository.save(any(StoreStatistics.class))).thenReturn(new StoreStatistics());

        // 태그 선택 Mock
        when(storeTagRepository.findByIdIn(tagIds)).thenReturn(List.of(sampleTag1, sampleTag2));

        // `findById()` Mock 추가
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));

        // When
        StoreDetailResponse response = storeService.createStore(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getAddress()).isEqualTo(request.getAddress());

        verify(storeRepository, times(1)).save(any(Store.class));
        verify(storeStatisticsRepository, times(1)).save(any(StoreStatistics.class));
        verify(storeRepository, times(1)).findById(1L); // ✅ `findById()`가 호출되었는지 검증
        verify(storeTagRepository, times(1)).findByIdIn(tagIds); // ✅ 선택한 태그 조회 검증
        verify(storeTagRelationRepository, times(1)).saveAll(anyList()); // ✅ 태그-가게 관계 저장 검증
    }

    /** 가게 간략 정보 조회 테스트 */
    @Test
    void getStoreSummary_Success() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(storeReviewRepository.findAverageRatingByStoreId(1L)).thenReturn(BigDecimal.valueOf(4.5));
        when(imageService.getImagesByTypeAndId(ImageType.STORE, 1L)).thenReturn(List.of("img1.jpg"));
        when(storeTagRelationRepository.findTagNamesByStoreId(1L)).thenReturn(List.of("Dessert", "Cafe"));

        // When
        StoreSummaryResponse response = storeService.getStoreSummary(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(sampleStore.getName());
        assertThat(response.getAverageRating()).isEqualTo(BigDecimal.valueOf(4.5));
        assertThat(response.getTags()).contains("Dessert", "Cafe");

        verify(storeRepository, times(1)).findById(1L);
        verify(storeReviewRepository, times(1)).findAverageRatingByStoreId(1L);
        verify(imageService, times(1)).getImagesByTypeAndId(ImageType.STORE, 1L);
        verify(storeTagRelationRepository, times(1)).findTagNamesByStoreId(1L);
    }

    /** 가게 상세 정보 조회 테스트 */
    @Test
    void getStoreDetails_Success() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(eventRepository.findByStoreId(1L)).thenReturn(List.of());
        when(couponRepository.findByStoreId(1L)).thenReturn(List.of());
        when(imageService.getImagesByTypeAndId(ImageType.STORE, 1L)).thenReturn(List.of("img1.jpg"));
        when(menuRepository.findByStoreId(1L)).thenReturn(List.of());
        when(storeReviewRepository.findByStoreId(1L)).thenReturn(List.of());
        when(storeTagRelationRepository.findTagNamesByStoreId(1L)).thenReturn(List.of("Dessert", "Cafe"));

        // When
        StoreDetailResponse response = storeService.getStoreDetails(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(sampleStore.getName());
        assertThat(response.getTags()).contains("Dessert", "Cafe");

        verify(storeRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findByStoreId(1L);
        verify(couponRepository, times(1)).findByStoreId(1L);
        verify(imageService, times(1)).getImagesByTypeAndId(ImageType.STORE, 1L);
        verify(menuRepository, times(1)).findByStoreId(1L);
        verify(storeReviewRepository, times(1)).findByStoreId(1L);
        verify(storeTagRelationRepository, times(1)).findTagNamesByStoreId(1L);
    }
}
