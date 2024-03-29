package il.tsv.test.playersservice.data;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * Represents a player entity in the database.
 */
@Data
@Entity
@Table(name="players")
public class Player {
    @Id
    private String playerID;
    private Integer birthYear;
    private Integer birthMonth;
    private Integer birthDay;
    private String birthCountry;
    private String birthState;
    private String birthCity;
    private Integer deathYear;
    private Integer deathMonth;
    private Integer deathDay;
    private String deathCountry;
    private String deathState;
    private String deathCity;
    private String nameFirst;
    private String nameLast;
    private String nameGiven;
    private Integer weight;
    private Integer height;
    @Enumerated(EnumType.STRING)
    private Side bats;
    @Enumerated(EnumType.STRING)
    private Side thrws;
    private Date debut;
    private Date finalGame;
    private String retroID;
    private String bbrefID;
}
