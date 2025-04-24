//package org.swyp.dessertbee.search.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.swyp.dessertbee.search.service.StoreSearchService;
//
//@Tag(name = "StoreSearch", description = "가게 검색 관련 API")
//@RestController
//@RequestMapping("/api/stores/search")
//@RequiredArgsConstructor
//public class StoreSearchController {
//
//    private final StoreSearchService storeSearchService;
//
//    /**
//     * (관리자 전용) 모든 가게 데이터를 Elasticsearch에 색인
//     */
//    @Operation(
//            summary = "(관리자) 가게 데이터 Elasticsearch 마이그레이션",
//            description = """
//                DB에 존재하는 모든 가게 데이터를 Elasticsearch에 일괄 색인합니다.
//                운영 중에는 자주 호출하지 않으며, 보통 초기 세팅 시 사용됩니다.
//                관리자(ROLE_ADMIN) 권한 필요
//                """
//    )
//    @ApiResponse(responseCode = "200", description = "Elasticsearch 색인 마이그레이션 완료")
//    @ApiResponse(responseCode = "500", description = "Elasticsearch 색인 중 오류 발생 (IOException 또는 연결 실패)")
//    @PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
//    @PostMapping("/migrate")
//    public ResponseEntity<String> migrateAllStoresToElasticsearch() {
//        storeSearchService.migrateStoresToElasticsearch();
//        return ResponseEntity.ok("Elasticsearch 색인 마이그레이션 완료");
//    }
//}
