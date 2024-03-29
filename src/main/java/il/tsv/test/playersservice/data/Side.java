package il.tsv.test.playersservice.data;

/*
 *  Represents the side of a player's batting or throwing hand.
 */
public enum Side {
    NOT_DEFINED, L, R, B;


    public static Side fromDTO(String str) {
        return str != null ? switch (str) {
            case "R" -> R;
            case "L" -> L;
            case "B" -> B;
            //case null-> NOT_DEFINED;
            default -> NOT_DEFINED;
        } : NOT_DEFINED;
    }
}
