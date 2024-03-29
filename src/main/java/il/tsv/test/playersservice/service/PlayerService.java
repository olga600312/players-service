package il.tsv.test.playersservice.service;

import il.tsv.test.playersservice.dto.PlayerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayerService {
    List<PlayerDTO> getAllPlayers();

    PlayerDTO getPlayerById(String id);

    Page<PlayerDTO> getAllPlayers(Pageable pageable);
}
