package il.tsv.test.playersservice.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.data.PlayerAWS;
import il.tsv.test.playersservice.util.CsvUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("aws")
@Slf4j
public class AWSInitializer {
    private final DynamoDBMapper dynamoDBMapper;
    @Value("${CSV_FILE_PATH}")
    private String fileName;

    private final CsvUtils csvUtils;
    private final MeterRegistry meterRegistry;

    public AWSInitializer(DynamoDBMapper dynamoDBMapper, CsvUtils csvUtils, MeterRegistry meterRegistry) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.csvUtils = csvUtils;
        this.meterRegistry = meterRegistry;
    }
    /**
     * The method reads the CSV file, converts its contents to a list of Player objects via CsvUtils utility class,
     * and saves the new data to the database.
     */
   // @PostConstruct
    private void init() {
        log.info("start Dynamo Init ");
        if (fileName != null) {
            Timer timer = meterRegistry.timer("timer.dbinit");
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                long t1 = System.currentTimeMillis();
                File initialFile = new File(fileName);
                if (initialFile.isFile()) {
                    log.info("call csvUtils");
                    List<Player> list = csvUtils.csvToPlayerList(initialFile);
                    // Scan the table to get all items
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

                    PaginatedScanList<PlayerAWS> scanResult = dynamoDBMapper.scan(PlayerAWS.class, scanExpression);
                    dynamoDBMapper.batchDelete(scanResult);
                    /*for (PlayerAWS item : scanResult) {
                        dynamoDBMapper.delete(item);
                        log.info("Deleted item: {}", item);
                    }*/
                    List<PlayerAWS> awsPlayers=list.stream().map(e->{
                        PlayerAWS p = new PlayerAWS();
                        BeanUtils.copyProperties(e, p);
                        return p;
                    }).toList();
                    dynamoDBMapper.batchSave(awsPlayers);
                }
                log.info("DbInit took {} sec", (System.currentTimeMillis() - t1) / 1000);
            }finally {
                sample.stop(timer);
            }
        }

    }
}
