package il.tsv.test.playersservice.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.mapper.PlayerMapper;
import il.tsv.test.playersservice.repository.PlayerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.pulsar.client.api.PulsarClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.pulsar.core.PulsarTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter counter;

    @Mock
    PulsarTemplate<PlayerDTO> pulsarTemplate;
    @Captor
    private ArgumentCaptor<String> acString;

    @Captor
    private ArgumentCaptor<Player> acPlayer;

    @InjectMocks
    private PlayerServiceImpl playerService;

    ListAppender<ILoggingEvent> listAppender;

    //  MockWebServer mockWebServer;


    @BeforeEach
    void setUp() {

        //  mockWebServer = new MockWebServer();
        // Initialize ListAppender for capturing logs
        Logger logger = (Logger) LoggerFactory.getLogger(PlayerServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }


    @Test
    void testGetAllPlayers() {
        //given
        List<Player> players = Arrays.asList(new Player(), new Player());

        //when
        when(playerRepository.findAll()).thenReturn(players);
        when(playerMapper.toPlayerDto(any())).thenReturn(new PlayerDTO());
        List<PlayerDTO> result = playerService.getAllPlayers();

        //then
        verify(playerRepository, times(1)).findAll();
        verify(playerMapper, times(2)).toPlayerDto(any());


        assertEquals(players.size(), result.size());
    }

    @Test
    void testGetPlayerById() {
        //given
        String PLAYER_ID = UUID.randomUUID().toString();
        Player player = new Player();

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setPlayerID(PLAYER_ID);


        //when
        when(playerRepository.findById(anyString())).thenReturn(java.util.Optional.of(player));
        when(playerMapper.toPlayerDto(player)).thenReturn(playerDTO);
        when(meterRegistry.counter(any(String.class), any(Iterable.class))).thenReturn(counter);
        PlayerDTO result = playerService.getPlayerById(PLAYER_ID);

        // then
        assertEquals(playerDTO, result);
        assertEquals(playerDTO.getPlayerID(), PLAYER_ID);

        verify(playerRepository, times(1)).findById(PLAYER_ID);
        verify(playerMapper, times(1)).toPlayerDto(player);

        verify(playerRepository).findById(acString.capture());
        verify(playerMapper).toPlayerDto(acPlayer.capture());

        assertEquals(PLAYER_ID, acString.getValue());
        assertEquals(player, acPlayer.getValue());


        // Verify interactions with meterRegistry
        verify(meterRegistry, times(1)).counter("get_player_by_id", List.of(Tag.of("id", PLAYER_ID)));
        verifyNoMoreInteractions(meterRegistry);
    }

    @Test
    void testGetAllPlayersWithPagination() {
        //given
        List<Player> players = Arrays.asList(new Player(), new Player());
        Page<Player> playerPage = new PageImpl<>(players);

        //when
        when(playerRepository.findAll(any(Pageable.class))).thenReturn(playerPage);
        when(playerMapper.toPlayerDto(any())).thenReturn(new PlayerDTO());
        when(meterRegistry.counter(any(String.class), any(Iterable.class))).thenReturn(counter);
        Page<PlayerDTO> result = playerService.getAllPlayers(mock(Pageable.class));

        //then

        assertEquals(players.size(), result.getContent().size());
        assertInstanceOf(PlayerDTO.class, result.getContent().get(0));

        verifyNoMoreInteractions(playerRepository);

        verify(playerRepository, times(1)).findAll(any(Pageable.class));
        verify(playerMapper, times(2)).toPlayerDto(any());
        // Verify interactions with meterRegistry
        verify(meterRegistry, times(1)).counter("get_player_all", List.of());
        verifyNoMoreInteractions(meterRegistry);
    }


    @Test
    void  testProduceNew_PlayerExists() {
        //given

        String id =UUID.randomUUID().toString();
        String name ="TestName";
        Player player=new Player();
        player.setPlayerID(id);
        player.setNameFirst(name);
        PlayerDTO dto=new PlayerDTO();
        dto.setPlayerID(id);
        dto.setNameFirst(name);

        //when
        when(playerMapper.toPlayer(any())).thenReturn(player);
        when(playerRepository.findById(anyString())).thenReturn(Optional.of(player));


        // then
        assertThrows(PulsarClientException.class, () -> playerService.produceNew(dto));
        verifyNoInteractions(pulsarTemplate);
        verify(playerRepository).findById(anyString());
        verifyNoMoreInteractions(playerRepository);



    }
    @Test
    void  testProduceNew() throws PulsarClientException {
        //given

        String id =UUID.randomUUID().toString();
        String name ="TestName";
        Player player=new Player();
        player.setPlayerID(id);
        player.setNameFirst(name);
        PlayerDTO dto=new PlayerDTO();
        dto.setPlayerID(id);
        dto.setNameFirst(name);

        //when
        when(playerMapper.toPlayer(any())).thenReturn(player);
        when(playerRepository.findById(anyString())).thenReturn(Optional.empty());
        when(playerRepository.save(player)).thenReturn(player);
        when(playerMapper.toPlayerDto(any(Player.class))).thenReturn(dto);

        playerService.produceNew(dto);
        // then
        verify(pulsarTemplate).send(any(),any(PlayerDTO.class));
        verify(playerRepository).save(any());
        verify(playerRepository).findById(anyString());
        verifyNoMoreInteractions(playerRepository);
        verify(playerMapper).toPlayerDto(acPlayer.capture());

        // Verify log messages
        verifyLogMessageContains("EventPublisher::publishPlayerMessage publish the event "+ id);
    }
    private void verifyLogMessageContains(String expectedLogMessage) {
        boolean found = listAppender.list.stream().anyMatch(event->event.getFormattedMessage().contains(expectedLogMessage));
        assert(found);
    }
}
