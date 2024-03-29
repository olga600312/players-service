package il.tsv.test.playersservice;

import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.service.PlayerService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@Transactional
class PlayersServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlayerService playerService;

    @BeforeAll
    static void setup() {
        // Set the system property CSV_FILE_PATH
        System.setProperty("CSV_FILE_PATH", "/path/to/csv/file.csv");
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testPlayerById_PlayerExists() throws Exception {
        String id = UUID.randomUUID().toString();
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setPlayerID(id);
        when(playerService.getPlayerById(id)).thenReturn(playerDTO);

        mockMvc.perform(get("/api/{id}", id))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.playerID", is(id)));
    }

    @Test
    void testGetPlayerById_PlayerNotFound() throws Exception {
        // Mocking the service method to return null, indicating player not found
        when(playerService.getPlayerById("999")).thenReturn(null);

        // Sending a GET request to "/api/999" endpoint
        mockMvc.perform(get("/api/999")
                        .header("Accept-Language", "en_US")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        content().json("{  \"errors\": [\"The player 999 not found\" ]}")
                );
    }
}
