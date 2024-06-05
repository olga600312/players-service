package il.tsv.test.playersservice.service;


import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.repository.PlayerRepository;
import il.tsv.test.playersservice.util.CsvUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Component responsible for initializing the DB with data from a CSV file.
 */
@Component
@Slf4j
public class DatabaseInitializer {
    // @Value("${csv.file.path}")
    @Value("${CSV_FILE_PATH}")
    private String fileName;
    private final PlayerRepository playerRepository;
    private final CsvUtils csvUtils;
    private final MeterRegistry meterRegistry;

    public DatabaseInitializer(PlayerRepository playerRepository, CsvUtils csvUtils, MeterRegistry meterRegistry) {
        this.playerRepository = playerRepository;
        this.csvUtils = csvUtils;
        this.meterRegistry = meterRegistry;
    }

    /**
     * The method reads the CSV file, converts its contents to a list of Player objects via CsvUtils utility class,
     * and saves the new data to the database.
     */
    @PostConstruct
    private void init() {
        log.info("start DbInit");
        if (fileName != null) {
            Timer timer = meterRegistry.timer("timer.dbinit");
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                long t1 = System.currentTimeMillis();
                File initialFile = new File(fileName);
                if (initialFile.isFile()) {
                    log.info("call csvUtils");
                    List<Player> list = csvUtils.csvToPlayerList(initialFile);
                    playerRepository.deleteAll();
                    playerRepository.saveAll(list);
                }
                log.info("DbInit took {} sec", (System.currentTimeMillis() - t1) / 1000);
            }finally {
                sample.stop(timer);
            }
        }

    }
}

