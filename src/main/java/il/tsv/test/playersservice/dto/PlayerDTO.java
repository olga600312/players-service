package il.tsv.test.playersservice.dto;

import il.tsv.test.playersservice.data.Side;
import lombok.Data;

import java.util.Date;

/**
 * Represents a DTO (Data Transfer Object) instead of directly exposing a Player entity in the API.
 * Contains data corresponding to Player entity fields.
 */
@Data
public class PlayerDTO {
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
    private Side bats;
    private Side thrws;
    private Date debut;
    private Date finalGame;
    private String retroID;
    private String bbrefID;
}
