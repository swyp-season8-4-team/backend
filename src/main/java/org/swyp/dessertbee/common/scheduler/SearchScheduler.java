package org.swyp.dessertbee.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.search.service.SearchService;

@Service
@RequiredArgsConstructor
public class SearchScheduler {

    private final SearchService searchService;

    @Scheduled(fixedRate = 60000)
    public void syncPopularSearchesToDB() {
        searchService.syncPopularSearchesToDB();
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul") // 매일 00:10 실행
    public void midnightSyncPopularSearchesToDB() {
        searchService.midnightSyncPopularSearchesToDB();
    }
}