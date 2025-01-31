package org.swyp.dessertbee.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.dto.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.StoreResponse;
import org.swyp.dessertbee.store.service.StoreService;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** 가게 등록 */
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody @Valid StoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(request));
    }

}
