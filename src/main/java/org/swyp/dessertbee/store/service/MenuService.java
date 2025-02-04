package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.dto.response.MenuResponse;
import org.swyp.dessertbee.store.entity.Menu;
import org.swyp.dessertbee.store.repository.MenuRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;

    /** 특정 가게의 특정 메뉴 조회 */
    public MenuResponse getMenuByStore(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 메뉴입니다."));
        return MenuResponse.fromEntity(menu);
    }

    /** 메뉴 추가 */
    public void addMenu(Long storeId, MenuCreateRequest request){
        Menu menu = Menu.builder()
                .storeId(storeId)
                .name(request.getName())
                .price(request.getPrice())
                .isPopular(request.getIsPopular())
                .description(request.getDescription())
                .build();
        menuRepository.save(menu);
    }

    /** 메뉴 수정 */
    public void updateMenu(Long menuId, MenuCreateRequest request){
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        menu.update(request.getName(), request.getPrice(), request.getIsPopular(), request.getDescription());
    }

    /** 메뉴 삭제 */
    public void deleteMenu(Long menuId){
        menuRepository.deleteById(menuId);
    }

    /** 특정 가게의 메뉴 목록 조회 */
    public List<MenuResponse> getMenusByStore(Long storeId) {
        List<Menu> menus = menuRepository.findByStoreId(storeId);
        return menus.stream().map(MenuResponse::fromEntity).collect(Collectors.toList());
    }
}
