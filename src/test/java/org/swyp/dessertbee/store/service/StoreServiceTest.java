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
    private final String s3BaseUrl = "https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/";

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
        List<String> imageFileNames = List.of("img1.jpg", "img2.jpg"); // ✅ 파일명만 저장
        String imagePath = "store/1/"; // ✅ 경로 추가

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
                .tagIds(tagIds)
                .menus(List.of())
                .events(List.of())
                .coupons(List.of())
                .storeImages(imageFileNames) // ✅ 파일명만 전달
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
        verify(storeRepository, times(1)).findById(1L);
        verify(storeTagRepository, times(1)).findByIdIn(tagIds);
        verify(storeTagRelationRepository, times(1)).saveAll(anyList());


        verify(imageService, times(1)).uploadAndSaveImages(imageFileNames, ImageType.STORE, 1L);
    }

    /** 가게 간략 정보 조회 테스트 */
    @Test
    void getStoreSummary_Success() {
        // Given
        List<String> imageFileNames = List.of("img1.jpg");
        List<String> expectedImageUrls = imageFileNames.stream()
                .map(fileName -> s3BaseUrl + "store/1/" + fileName)
                .toList();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        lenient().when(storeReviewRepository.findAverageRatingByStoreId(1L)).thenReturn(BigDecimal.valueOf(4.5)); // ✅ lenient 적용
        when(imageService.getImagesByTypeAndId(ImageType.STORE, 1L)).thenReturn(expectedImageUrls);
        when(storeTagRelationRepository.findTagNamesByStoreId(1L)).thenReturn(List.of("Dessert", "Cafe"));

        // When
        StoreSummaryResponse response = storeService.getStoreSummary(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(sampleStore.getName());
        assertThat(response.getAverageRating()).isEqualTo(BigDecimal.valueOf(4.5));
        assertThat(response.getStoreImages()).isEqualTo(expectedImageUrls);
        assertThat(response.getTags()).contains("Dessert", "Cafe");

        verify(imageService, times(1)).getImagesByTypeAndId(ImageType.STORE, 1L);
    }

    /** 가게 상세 정보 조회 테스트 */
    @Test
    void getStoreDetails_Success() {
        List<String> imageFileNames = List.of("img1.jpg");
        List<String> expectedImageUrls = imageFileNames.stream()
                .map(fileName -> s3BaseUrl + "store/1/" + fileName)
                .toList();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));
        when(eventRepository.findByStoreId(1L)).thenReturn(List.of());
        when(couponRepository.findByStoreId(1L)).thenReturn(List.of());
        when(imageService.getImagesByTypeAndId(ImageType.STORE, 1L)).thenReturn(expectedImageUrls);
        when(menuRepository.findByStoreId(1L)).thenReturn(List.of());
        when(storeReviewRepository.findByStoreId(1L)).thenReturn(List.of());
        when(storeTagRelationRepository.findTagNamesByStoreId(1L)).thenReturn(List.of("Dessert", "Cafe"));

        // When
        StoreDetailResponse response = storeService.getStoreDetails(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(sampleStore.getName());
        assertThat(response.getStoreImages()).isEqualTo(expectedImageUrls);
        assertThat(response.getTags()).contains("Dessert", "Cafe");

        verify(imageService, times(1)).getImagesByTypeAndId(ImageType.STORE, 1L);
    }
}
