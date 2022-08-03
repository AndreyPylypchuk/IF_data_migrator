package com.tht.ifdatamigrator;

import com.tht.ifdatamigrator.service.BackupService;
import com.tht.ifdatamigrator.service.RestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class IfDataMigratorApplication implements CommandLineRunner {

    @Value("${migration.task}")
    private MigrationTask task;

    public static void main(String[] args) {
        SpringApplication.run(IfDataMigratorApplication.class, args);
    }

    private final BackupService backupService;
    private final RestoreService restoreService;

    @Override
    public void run(String... args) {
        switch (task) {
            case backup -> backupService.backup();
            case restore -> restoreService.restore();
        }
    }
}
