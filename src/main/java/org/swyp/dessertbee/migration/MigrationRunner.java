package org.swyp.dessertbee.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.migration.service.StoreLinkMigrationService;

@Component
@RequiredArgsConstructor
public class MigrationRunner implements CommandLineRunner {

    private final StoreLinkMigrationService migrationService;

    @Override
    public void run(String... args) {
        migrationService.migrateStoreLinks();
    }
}
