package il.tsv.test.playersservice.controller;

import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.error.ErrorsPresentation;
import il.tsv.test.playersservice.service.PlayerService;
import org.apache.pulsar.client.api.PulsarClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static il.tsv.test.playersservice.utils.TestUtils.getPlayerDTOS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {
    @Mock
    PlayerService playerService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    PlayerController controller;

    @Test
    void getAllPlayers() {
        //given
        List<PlayerDTO> list = getPlayerDTOS();

        //when
        when(playerService.getAllPlayers()).thenReturn(list);
        var responseEntity = controller.getAllPlayers();

        //then
        List<PlayerDTO> body = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertInstanceOf(List.class, body);
        assertNotNull(body);
        assertEquals(list.size(), body.size());
        assertInstanceOf(PlayerDTO.class, body.get(0));
        assertNotNull(body.get(0).getPlayerID());
        verify(playerService).getAllPlayers();

        verifyNoMoreInteractions(playerService);
    }

    @Test
    void getPageablePlayers() {
        //given
        List<PlayerDTO> list = getPlayerDTOS();
        //Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "playerID"));
        Pageable pageable = PageRequest.of(0, 20);
        Page<PlayerDTO> page = new PageImpl<>(list, pageable, list.size());

        // when
        when(playerService.getAllPlayers(any(Pageable.class))).thenReturn(page);
        ResponseEntity<?> responseEntity = controller.getPageablePlayers(0, 20, Locale.US);

        //then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        if (responseEntity.getBody() instanceof Page<?> p) {
            assertEquals(p.getTotalElements(), list.size());
            assertEquals(p.getNumber(), 0);
        } else
            assertInstanceOf(Page.class, responseEntity.getBody());

        verify(playerService).getAllPlayers(pageable);
        verifyNoMoreInteractions(playerService);

    }

    @Test
    void getPageablePlayers_IllegalSize() {
        //given
        var locale = Locale.US;
        var errorMessage = "Page size must not be less than one";
        // when
        doReturn(errorMessage).when(messageSource)
                .getMessage("illegal.size.value", new Object[0], locale);


        ResponseEntity<?> responseEntity = controller.getPageablePlayers(0, -20, Locale.US);

        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        if (responseEntity.getBody() instanceof ErrorsPresentation errors) {
            assertNotNull(errors);
            assertFalse(errors.errors().isEmpty());
            assertEquals(errorMessage, errors.errors().get(0));
        } else {
            assertInstanceOf(ErrorsPresentation.class, responseEntity.getBody());
        }
        verifyNoInteractions(playerService);
    }

    @Test
    void getPageablePlayers_IllegalPageNumber() {
        //given
        var locale = Locale.US;
        var errorMessage = "Page number must not be less than zero";

        // when
        doReturn(errorMessage).when(messageSource)
                .getMessage("illegal.page.value", new Object[0], locale);


        ResponseEntity<?> responseEntity = controller.getPageablePlayers(-1, 20, Locale.US);

        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        if (responseEntity.getBody() instanceof ErrorsPresentation errors) {
            assertNotNull(errors);
            assertFalse(errors.errors().isEmpty());
            assertEquals(errorMessage, errors.errors().get(0));
        } else {
            assertInstanceOf(ErrorsPresentation.class, responseEntity.getBody());
        }
        verifyNoInteractions(playerService);
    }

    @Test
    void getPlayerById_PlayerExists() {
        //given
        String playerID = UUID.randomUUID().toString();
        PlayerDTO dto = new PlayerDTO();
        dto.setPlayerID(playerID);


        //when
        when(playerService.getPlayerById(playerID)).thenReturn(dto);
        ResponseEntity<?> responseEntity = controller.getPlayerById(playerID, Locale.US);

        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(responseEntity.getBody());
        assertInstanceOf(PlayerDTO.class, responseEntity.getBody());
        assertEquals(((PlayerDTO) responseEntity.getBody()).getPlayerID(), playerID);

        verify(playerService).getPlayerById(playerID);
        verifyNoMoreInteractions(playerService);
    }

    @Test
    void getPlayerById_PlayerNotExists() {
        //given
        String playerID = "1";

        var locale = Locale.US;
        var errorMessage = "The player 1 not found";


        //when
        when(playerService.getPlayerById(playerID)).thenReturn(null);
        when(messageSource.getMessage(eq("player.not.found"), eq(new String[]{playerID}), anyString(), eq(locale)))
                .thenReturn(errorMessage);

        ResponseEntity<?> responseEntity = controller.getPlayerById(playerID, locale);

        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        if (responseEntity.getBody() instanceof ErrorsPresentation errors) {
            assertNotNull(errors);
            assertFalse(errors.errors().isEmpty());
            assertEquals(errorMessage, errors.errors().get(0));
        } else {
            assertInstanceOf(ErrorsPresentation.class, responseEntity.getBody());
        }

        verify(playerService).getPlayerById(playerID);
        verifyNoMoreInteractions(playerService);
    }

    @Test
    void testProduceNewPlayerEvent() throws PulsarClientException {
        //given
        String id = UUID.randomUUID().toString();
        String name = "TestName";

        PlayerDTO dto = new PlayerDTO();
        dto.setPlayerID(id);
        dto.setNameFirst(name);

        //when
        when(playerService.produceNew(dto)).thenReturn(id);

        ResponseEntity<?> responseEntity = controller.produceNewPlayerEvent(dto);
        //then
        verify(playerService).produceNew(any(PlayerDTO.class));
        assertEquals(responseEntity.getStatusCode(), HttpStatusCode.valueOf(200));
        assertEquals(responseEntity.getBody(), id);
        assertEquals(responseEntity.getHeaders().getContentType(), MediaType.TEXT_PLAIN);

    }

    @Test
    void testProduceNewPlayerEvent_PlayerExists() throws PulsarClientException {
        //given
        String id = UUID.randomUUID().toString();
        String name = "TestName";

        PlayerDTO dto = new PlayerDTO();
        dto.setPlayerID(id);
        dto.setNameFirst(name);

        ErrorsPresentation errorsPresentation=new ErrorsPresentation(List.of("Cannot post new Player DTO"));

        //when
        when(playerService.produceNew(dto)).thenReturn(null);

        ResponseEntity<?> responseEntity = controller.produceNewPlayerEvent(dto);
        //then
        verify(playerService).produceNew(any(PlayerDTO.class));
        assertEquals(responseEntity.getStatusCode(), HttpStatusCode.valueOf(500));
        assertEquals(responseEntity.getBody(),errorsPresentation );
        assertEquals(responseEntity.getHeaders().getContentType(), MediaType.APPLICATION_JSON);

    }

}