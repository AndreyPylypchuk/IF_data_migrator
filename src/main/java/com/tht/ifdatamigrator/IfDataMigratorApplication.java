package com.tht.ifdatamigrator;

import com.tht.ifdatamigrator.service.BackupService;
import com.tht.ifdatamigrator.service.RestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class IfDataMigratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IfDataMigratorApplication.class, args);
    }

    private final BackupService backupService;
    private final RestoreService restoreService;

    @Bean
    public CommandLineRunner run(@Value("${migration.task}") MigrationTask task) {
        return (args) -> {
            switch (task) {
                case backup -> backupService.backup();
                case restore -> restoreService.restore();
            }
        };
    }
}
