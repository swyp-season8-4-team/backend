package org.swyp.dessertbee.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.service.SearchService;

@Service
@RequiredArgsConstructor
public class SearchScheduler {

    private final SearchService searchService;

    @Scheduled(fixedRate = 60000)
    public void syncPopularSearchesToDB() {
        searchService.syncPopularSearchesToDB();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void midnightSyncPopularSearchesToDB() {
        searchService.midnightSyncPopularSearchesToDB();
    }
}