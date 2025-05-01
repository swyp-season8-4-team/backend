package org.swyp.dessertbee.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.store.preference.service.StoreTopTagBatchTriggerService;

@Tag(name = "Batch Test", description = "배치 작업 수동 실행 API")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchTestController {

    private final StoreTopTagBatchTriggerService storeTopTagBatchTriggerService;

    @Operation(
            summary = "Store Top Tag 배치 수동 실행",
            description = "store_top_tag 테이블을 초기화하고, 가게별 Top 3 태그를 재집계하는 배치 작업을 수동으로 실행합니다."
    )
    @ApiResponse(responseCode = "200", description = "Store Top Tag 배치 수동 실행 완료")
    @PostMapping("/store-top-tag")
    public String runStoreTopTagBatch() {
        storeTopTagBatchTriggerService.executeBatch();
        return "Store Top Tag 배치 수동 실행 완료!";
    }
}