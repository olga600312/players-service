package il.tsv.test.playersservice.repository;

import il.tsv.test.playersservice.data.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerRepositoryTest {
    @Autowired
    PlayerRepository repository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @Transactional
    @Rollback
    void findPlayerByPlayerID() {
        // given
        String name = "test1";
        String nameLast = "test1@example.com";
        String id = UUID.randomUUID().toString();

        // Create a Player object with the test data
        Player player = new Player();
        player.setPlayerID(id);
        player.setNameFirst(name);
        player.setNameLast(nameLast);

        //when
        repository.save(player);
        Player saved=repository.findPlayerByPlayerID(id);

        //then

        assertEquals(player.getNameFirst(),saved.getNameFirst());

    }
}