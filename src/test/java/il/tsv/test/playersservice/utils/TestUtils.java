package il.tsv.test.playersservice.utils;

import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.dto.PlayerDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestUtils {
    public static List<PlayerDTO> getPlayerDTOS() {
        List<PlayerDTO> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PlayerDTO dto = new PlayerDTO();
            dto.setPlayerID(UUID.randomUUID().toString());
            list.add(dto);
        }
        return list;
    }
    public static List<Player> getPlayers() {
        List<Player> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Player player = new Player();
            player.setPlayerID(UUID.randomUUID().toString());
            list.add(player);
        }
        return list;
    }
}
