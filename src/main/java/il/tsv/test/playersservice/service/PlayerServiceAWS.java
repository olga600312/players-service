package il.tsv.test.playersservice.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.data.PlayerAWS;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.mapper.PlayerMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PlayerService implementation class for receiving player data.
 */
@Service
@Slf4j
@Profile("aws")
public class PlayerServiceAWS implements PlayerService {
    private final DynamoDBMapper dynamoDBMapper;
    private final PlayerMapper playerMapper;
    private final MeterRegistry meterRegistry;
    @Value("${spring.pulsar.producer.topic-name}")
    private String topicName;
    private final PulsarTemplate<PlayerDTO> pulsarTemplate;
    private final PulsarTemplate<byte[]> pulsarByteTemplate;


    public PlayerServiceAWS(DynamoDBMapper dynamoDBMapper, PlayerMapper playerMapper, MeterRegistry meterRegistry, PulsarTemplate<PlayerDTO> pulsarTemplate, PulsarTemplate<byte[]> pulsarByteTemplate) {
        this.dynamoDBMapper = dynamoDBMapper;

        this.playerMapper = playerMapper;
        this.meterRegistry = meterRegistry;
        this.pulsarTemplate = pulsarTemplate;
        this.pulsarByteTemplate = pulsarByteTemplate;
        log.info("PlayerService from default profile");
    }

    /**
     * Retrieves a list of all players.
     *
     * @return List of PlayerDTO representing all players.
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        // Scan the table
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<PlayerAWS> list = dynamoDBMapper.scan(PlayerAWS.class, scanExpression);

        return list.stream().map(e->{
            PlayerDTO dto=new PlayerDTO();
            BeanUtils.copyProperties(e, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves a player by their ID.
     *
     * @param id ID of the player to retrieve.
     * @return PlayerDTO representing the player with the specified ID, or null if not found.
     */
    @Override
    public PlayerDTO getPlayerById(String id) {
        PlayerAWS player = dynamoDBMapper.load(PlayerAWS.class, id);
        meterRegistry.counter("get_player_by_id", List.of(Tag.of("id", id))).increment();


        return player != null ? playerMapper.toPlayerAWSDto(player) : null;
    }

    @Override
    public String produceNew(PlayerDTO dto) throws PulsarClientException {
        String id = null;
        PlayerAWS p=playerMapper.toPlayerAWS(dto);
        if(p!=null) {
            id=p.getPlayerID();
            PlayerAWS old = dynamoDBMapper.load(PlayerAWS.class, id);
            if(old!=null){
                throw new PulsarClientException(String.format("The player %s already exists",id));
            }

            dynamoDBMapper.save(p);
            pulsarTemplate.send(topicName, playerMapper.toPlayerAWSDto(p));
            pulsarByteTemplate
                    .newMessage(null)
                    .withTopic("null_value_topic")
                    .withSchema(Schema.BYTES)
                    .withMessageCustomizer((mb) -> mb.key("key:1234"))
                    .send();
            log.info("EventPublisher::publishPlayerMessage publish the event {}", id);
        }
        return id;

    }

    /**
     * Retrieves a page list of players specified by the pageable parameter.
     * There is no direct way of identifying the page number. We have to keep track of the total number of items and calculate the pages manually.
     * Still, keep in mind that the number of items for a page could vary if you have filter parameters defined and for the last page.
     * Therefore, continuous pagination (or from an application perspective infinite scrolling of the next page) is recommended.
     * @param pageable Pageable object specifying the page number and size.
     * @return Page<PlayerDTO> representing a page of player(dto) data.
     */
    @Override
    public Page<PlayerDTO> getAllPlayers(Pageable pageable) {
        meterRegistry.counter("get_player_all", List.of()).increment();
        Timer timer = meterRegistry.timer("timer.players.all");
        Timer.Sample sample = Timer.start(meterRegistry);
        Page<Player> page;
        List<PlayerDTO> playerDTOs;
        try {
            // Pagination parameters
            int pageSize = pageable.getPageSize(); // Number of items per page
            Map<String, AttributeValue> lastEvaluatedKey = null;
            // Scan expression with pagination
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withLimit(pageSize)
                    //.withSegment(pageable.getPageNumber())
                    .withExclusiveStartKey(lastEvaluatedKey)
                    ;
// Perform the scan
            PaginatedScanList<PlayerAWS> paginatedScanList = dynamoDBMapper.scan(PlayerAWS.class, scanExpression);
            // Get the last evaluated key to continue the pagination
          //  lastEvaluatedKey = paginatedScanList..getLast();

            playerDTOs = paginatedScanList.stream()
                    .map(playerMapper::toPlayerAWSDto)
                    .collect(Collectors.toList());
        } finally {
            sample.stop(timer);

        }

        return new PageImpl<>(playerDTOs, pageable, playerDTOs.size());
    }
}
