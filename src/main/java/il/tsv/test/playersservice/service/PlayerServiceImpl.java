package il.tsv.test.playersservice.service;

import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.mapper.PlayerMapper;
import il.tsv.test.playersservice.repository.PlayerRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PlayerService implementation class for receiving player data.
 */
@Service
@AllArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private PlayerRepository playerRepository;
    private PlayerMapper playerMapper;
    private MeterRegistry meterRegistry;

    /**
     * Retrieves a list of all players.
     *
     * @return List of PlayerDTO representing all players.
     */
    @Override
    public List<PlayerDTO> getAllPlayers() {
        List<Player> list = playerRepository.findAll();
        return list.stream().map(e -> playerMapper.toPlayerDto(e)).collect(Collectors.toList());
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
                .map(e->playerMapper.toPlayerDto(e))
                .collect(Collectors.toList());
        return new PageImpl<>(playerDTOs, page.getPageable(), page.getTotalElements());
    }
}
