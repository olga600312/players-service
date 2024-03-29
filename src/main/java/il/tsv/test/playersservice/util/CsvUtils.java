package il.tsv.test.playersservice.util;


import il.tsv.test.playersservice.data.Player;
import il.tsv.test.playersservice.data.Side;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility component for parsing CSV file and converting it content into a list of Player objects.
 */
@Component
public class CsvUtils {


    public List<Player> csvToPlayerList(File file) {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(bReader,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(CsvHeader.names())
                             .build())) {
            List<Player> list = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            Map<String, SimpleDateFormat> dateFormatMap = new TreeMap<>();

            for (CSVRecord r : csvRecords) {
                if (r.getRecordNumber() > 1) {
                    Player p = new Player();
                    p.setPlayerID(r.get(CsvHeader.playerID));
                    p.setBirthYear(asInteger(r, CsvHeader.birthYear));
                    p.setBirthMonth(asInteger(r, CsvHeader.birthMonth));
                    p.setBirthDay(asInteger(r, CsvHeader.birthDay));
                    p.setBirthCountry(r.get(CsvHeader.birthCountry));
                    p.setBirthState(r.get(CsvHeader.birthState));
                    p.setBirthCity(r.get(CsvHeader.birthCity));
                    p.setDeathYear(asInteger(r, CsvHeader.deathYear));
                    p.setDeathMonth(asInteger(r, CsvHeader.deathMonth));
                    p.setDeathDay(asInteger(r, CsvHeader.deathDay));
                    p.setDeathCountry(r.get(CsvHeader.deathCountry));
                    p.setDeathState(r.get(CsvHeader.deathState));
                    p.setDeathCity(r.get(CsvHeader.deathCity));
                    p.setNameFirst(r.get(CsvHeader.nameFirst));
                    p.setNameLast(r.get(CsvHeader.nameLast));
                    p.setNameGiven(r.get(CsvHeader.nameGiven));
                    p.setWeight(asInteger(r, CsvHeader.weight));
                    p.setHeight(asInteger(r, CsvHeader.height));
                    p.setBats(Side.fromDTO(r.get(CsvHeader.bats)));
                    p.setThrws(Side.fromDTO(r.get(CsvHeader.thrws.headerName())));
                    p.setDebut(asDate(r, CsvHeader.debut, dateFormatMap));
                    p.setFinalGame(asDate(r, CsvHeader.finalGame, dateFormatMap));
                    p.setRetroID(r.get(CsvHeader.retroID));
                    p.setBbrefID(r.get(CsvHeader.bbrefID));
                    list.add(p);
                }
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException("CSV data is failed to parse: " + e.getMessage());
        }
    }

    private  Date asDate(CSVRecord r, CsvHeader header, Map<String, SimpleDateFormat> dateFormatMap) {
        String str = r.get(header);
        Date date = null;
        if (!isBlank(str)) {
            try {
                if (str.matches("^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")) {
                    date = dateFormatMap.computeIfAbsent("-", (k) -> new SimpleDateFormat("yyyy-MM-dd")).parse(str);
                } else if (str.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/\\d{4}$"))
                    date = dateFormatMap.computeIfAbsent("-", (k) -> new SimpleDateFormat("dd/MM/yyyy")).parse(str);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return date;
    }

    public  boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public  boolean isNumber(String str) {
        return !isBlank(str) && str.matches("[-+]?\\d+");
    }

    public  Integer asInteger(CSVRecord csvRecord, CsvHeader header) {
        String str = csvRecord.get(header);
        return isNumber(str) ? Integer.parseInt(str.trim()) : null;
    }

}

