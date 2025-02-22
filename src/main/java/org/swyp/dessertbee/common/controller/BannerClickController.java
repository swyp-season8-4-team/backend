package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.service.BannerClickService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BannerClickController {

    private final BannerClickService bannerClickService;

    @PostMapping("/banners/click")
    public ResponseEntity<Void> increaseBannerClick() {
        bannerClickService.increaseBannerClick();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/banners/clicks")
    public ResponseEntity<Integer> getBannerClickCount() {
        return ResponseEntity.ok(bannerClickService.getBannerClickCount());
    }
}
