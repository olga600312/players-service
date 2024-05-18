package il.tsv.test.playersservice.repository;

import il.tsv.test.playersservice.data.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player,String> {
   Player findPlayerByPlayerID(String id);
}
