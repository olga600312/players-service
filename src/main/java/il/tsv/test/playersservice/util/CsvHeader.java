package il.tsv.test.playersservice.util;

/**
 * Represents the header names for CSV files related to player data.
 */
public enum CsvHeader {
    playerID, birthYear, birthMonth, birthDay, birthCountry, birthState, birthCity, deathYear, deathMonth, deathDay, deathCountry, deathState, deathCity, nameFirst, nameLast, nameGiven, weight, height, bats,
    thrws{
        /**
         *  Overrides the headerName method for the thrws enum value to return "throws".
         *  The "thrws" is used here instead of "throws" due to the fact that "throws" is a reserved word in Java.
         *
         * @return The header name as "throws".
         */
        public String headerName(){
            return "throws";
        }
    }, debut, finalGame, retroID, bbrefID;

    public static String[] names() {
        CsvHeader[] values=values();
        String[] names=new String[values.length];
        for(int i=0;i<values.length;i++){
            names[i]=values[i].headerName();
        }
        return names;
    }

    public String headerName(){
        return name();
    }


}
