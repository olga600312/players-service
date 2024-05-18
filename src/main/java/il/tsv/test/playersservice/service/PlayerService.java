package il.tsv.test.playersservice.service;

import il.tsv.test.playersservice.dto.PlayerDTO;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayerService {
    List<PlayerDTO> getAllPlayers();

    PlayerDTO getPlayerById(String id);
    String produceNew(PlayerDTO dto) throws PulsarClientException;

    Page<PlayerDTO> getAllPlayers(Pageable pageable);
}
