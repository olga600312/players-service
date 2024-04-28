package il.tsv.test.playersservice.service;

import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.repository.PlayerRepository;
import il.tsv.test.playersservice.util.CsvUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseInitializerTest {
    @Mock
    PlayerRepository playerRepository;
    @Mock
    CsvUtils csvUtils;


    @InjectMocks
    DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        // Inject the value for CSV_FILE_PATH
    }

    @Test
    void testInit() {
        //given
        ReflectionTestUtils.setField(databaseInitializer, "fileName", "D:/player.csv");

        //File initialFile = mock(File.class);
        File initialFile = new File((String) ReflectionTestUtils.getField(databaseInitializer, "fileName"));
        List<Player> list = List.of(new Player());


        //when
        when(csvUtils.csvToPlayerList(initialFile)).thenReturn(list);
        doAnswer((i) -> null).when(playerRepository).deleteAll();
        when(playerRepository.saveAll(list)).thenReturn(list);
        ReflectionTestUtils.invokeMethod(databaseInitializer, "init");

        //then
        verify(csvUtils).csvToPlayerList(initialFile);
        verify(playerRepository).deleteAll();
        verify(playerRepository).saveAll(list);
        verifyNoMoreInteractions(playerRepository);


    }

    @Test
    void testInit_InvalidFile() {
        //given
        ReflectionTestUtils.setField(databaseInitializer, "fileName", "dummy.csv");


        //when
        ReflectionTestUtils.invokeMethod(databaseInitializer, "init");

        //then
        verifyNoInteractions(playerRepository);
        verifyNoInteractions(csvUtils);


    }
}