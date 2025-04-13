package org.swyp.dessertbee.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.elasticsearch.document.StoreSearchDocument;
import org.swyp.dessertbee.elasticsearch.service.StoreSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/stores/es")
@RequiredArgsConstructor
public class StoreSearchController {

    private final StoreSearchService storeSearchService;

    @GetMapping("/map")
    public ResponseEntity<List<StoreSearchDocument>> searchStores(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam String searchKeyword
    ) {
        return ResponseEntity.ok(storeSearchService.search(latitude, longitude, radius, searchKeyword));
    }
}