package org.swyp.dessertbee.store.store.dto.request;

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

    private StoreStatus status = StoreStatus.APPROVED;
    private List<MenuCreateRequest> menus;
    private List<MultipartFile> storeImageFiles;
    private List<MultipartFile> ownerPickImageFiles;
    private Map<String, MultipartFile> menuImageFiles;

    // StoreLinkRequest 클래스 재정의 없이 상속받아 사용
}