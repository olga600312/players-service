package il.tsv.test.playersservice.mapper;


import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.data.PlayerAWS;
import il.tsv.test.playersservice.dto.PlayerDTO;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for mapping Player entities to PlayerDTO dto and vice versa.
 */
@Component
@Slf4j
public  class PlayerMapper {
    private final ModelMapper modelMapper;

    public PlayerMapper() {
        modelMapper = new ModelMapper();
    }

    public  PlayerDTO toPlayerDto(Player player){
        return modelMapper.map(player, PlayerDTO.class);
    }
    public  Player toPlayer(PlayerDTO dto) {
        return modelMapper.map(dto, Player.class);
    }
    public  PlayerDTO toPlayerAWSDto(PlayerAWS player){
        return modelMapper.map(player, PlayerDTO.class);
    }
    public  PlayerAWS toPlayerAWS(PlayerDTO dto) {
        return modelMapper.map(dto, PlayerAWS.class);
    }
}
