package il.tsv.test.playersservice.data;

public enum Bats {
    NOT_DEFINED {
        @Override
        public String toDTO() {
            return "";
        }
    }, LEFT {
        @Override
        public String toDTO() {
            return "L";
        }
    }, RIGHT {
        @Override
        public String toDTO() {
            return "R";
        }
    }, BOTH {
        @Override
        public String toDTO() {
            return "B";
        }
    };

    public abstract String toDTO();

    public static Bats fromDTO(String str) {
        return switch (str) {
            case "R" -> RIGHT;
            case "L" -> LEFT;
            case "B" -> BOTH;
            default -> NOT_DEFINED;
        };
    }
}
