package org.swyp.dessertbee.store.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.service.SavedStoreService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SavedStoreController {

    private final SavedStoreService savedStoreService;

    /** 유저별 저장된 가게 조회 */
    @GetMapping("/{userId}/saved-stores")
    public List<SavedStoreResponse> getSavedStoresByUser(@PathVariable Long userId) {
        return savedStoreService.getSavedStoresByUser(userId);
    }
}
