package il.tsv.test.playersservice.service;

import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.mapper.PlayerMapper;
import il.tsv.test.playersservice.repository.PlayerRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PlayerService implementation class for receiving player data.
 */
@Service
@Slf4j
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final MeterRegistry meterRegistry;
    @Value("${spring.pulsar.producer.topic-name}")
    private String topicName;
    private PulsarTemplate<Object> pulsarTemplate;


    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerMapper playerMapper, MeterRegistry meterRegistry, PulsarTemplate<Object> pulsarTemplate) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.meterRegistry = meterRegistry;
        this.pulsarTemplate = pulsarTemplate;
    }

    /**
     * Retrieves a list of all players.
     *
     * @return List of PlayerDTO representing all players.
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        List<Player> list = playerRepository.findAll();
        return list.stream().map(playerMapper::toPlayerDto).collect(Collectors.toList());
    }

    /**
     * Retrieves a player by their ID.
     *
     * @param id ID of the player to retrieve.
     * @return PlayerDTO representing the player with the specified ID, or null if not found.
     */
    @Override
    public PlayerDTO getPlayerById(String id) {
        Player player = playerRepository.findById(id).orElse(null);
        meterRegistry.counter("get_player_by_id", List.of(Tag.of("id", id))).increment();


        return player != null ? playerMapper.toPlayerDto(player) : null;
    }

    @Override
    public String produceNew(PlayerDTO dto) throws PulsarClientException {
        String id = null;
        Player p=playerMapper.toPlayer(dto);
        if(p!=null) {
            id=p.getPlayerID();
            Player old=playerRepository.findById(id).orElse(null);
            if(old!=null){
                throw new PulsarClientException(String.format("The player %s already exists",id));
            }
            Player saved=playerRepository.save(p);
            pulsarTemplate.send(topicName, playerMapper.toPlayerDto(saved));
            log.info("EventPublisher::publishPlayerMessage publish the event {}", id);
        }
        return id;

    }

    /**
     * Retrieves a page list of players specified by the pageable parm.
     *
     * @param pageable Pageable object specifying the page number and size.
     * @return Page<PlayerDTO> representing a page of player(dto) data.
     */
    @Override
    public Page<PlayerDTO> getAllPlayers(Pageable pageable) {
        meterRegistry.counter("get_player_all", List.of()).increment();
        Page<Player> page = playerRepository.findAll(pageable);
        List<PlayerDTO> playerDTOs = page.getContent().stream()
                .map(e -> playerMapper.toPlayerDto(e))
                .collect(Collectors.toList());
        return new PageImpl<>(playerDTOs, page.getPageable(), page.getTotalElements());
    }
}
