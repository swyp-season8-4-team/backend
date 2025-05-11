package org.swyp.dessertbee.store.menu.converter;

import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;

/**
 * 메뉴 변환 유틸리티 클래스
 */
public class MenuConverter {
    /**
     * MenuRequest를 MenuCreateRequest로 변환
     */
    public static MenuCreateRequest convertToMenuCreateRequest(StoreCreateRequest.MenuRequest menuRequest) {
        return new MenuCreateRequest(
                menuRequest.getName(),
                menuRequest.getPrice(),
                menuRequest.getIsPopular(),
                menuRequest.getDescription(),
                menuRequest.getImageFileKey()
        );
    }
}