package org.swyp.dessertbee.store.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.store.entity.StoreStatus;

import java.util.List;
import java.util.Map;

/**
 * 가게 생성 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest extends BaseStoreRequest {

    @Builder.Default
    @Schema(description = "가게 상태 (PENDING, APPROVED, REJECTED), 기본값 설정되어 있으므로 프론트에서 따로 보낼 필요 없음")
    private StoreStatus status = StoreStatus.APPROVED;

    @Schema(description = "등록할 메뉴 정보 목록")
    private List<MenuCreateRequest> menus;

    @Schema(description = "가게 대표 이미지 파일 목록")
    private List<MultipartFile> storeImageFiles;

    @Schema(description = "업주가 선택한 추가 이미지 파일 목록")
    private List<MultipartFile> ownerPickImageFiles;

    @Schema(description = "등록할 메뉴 이미지 파일 목록")
    private Map<String, MultipartFile> menuImageFiles;

    // StoreLinkRequest 클래스 재정의 없이 상속받아 사용
}