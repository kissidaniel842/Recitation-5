public class EmailValidator {
    public static final String PRINTABLE_CHAR = "!#$%&'*+-/=?^_`{|}~/";
    public static final char DOT = '.';
    public static final char HYPHEN = '-';
    public static final char AT = '@';

    // Enumerated states for FSM
    private enum State {
        START, LOCAL, DOT_LOCAL, AT_SYMBOL, DOMAIN, DOT_DOMAIN, INVALID
    }

    public static boolean isEmailValid(String address) {
        if (address == null || address.isEmpty() || address.length() > 320) return false;

        State state = State.START;
        int domainLength = 0, labelLength = 0, localLength = 0;
        boolean hasAtSymbol = false;
        boolean hasDotInDomain = false;
        boolean isLocalhost = false;

        for (int i = 0; i < address.length(); i++) {
            char ch = address.charAt(i);

            switch (state) {
                case START:
                    if (isValidLocalChar(ch)) {
                        state = State.LOCAL;
                        localLength++;
                    } else {
                        return false;
                    }
                    break;

                case LOCAL:
                    if (localLength >= 64) return false; // Local part limit
                    if (ch == DOT) {
                        if (i == 0 || address.charAt(i - 1) == DOT) return false; // No consecutive dots, no leading dot
                        state = State.DOT_LOCAL;
                    } else if (ch == AT) {
                        if (i == 0 || address.charAt(i - 1) == DOT) return false; // No empty or dot-ending local part
                        state = State.AT_SYMBOL;
                        hasAtSymbol = true;
                    } else if (!isValidLocalChar(ch)) {
                        return false;
                    } else {
                        localLength++;
                    }
                    break;

                case DOT_LOCAL:
                    if (isValidLocalChar(ch)) {
                        state = State.LOCAL;
                        localLength++;
                    } else {
                        return false;
                    }
                    break;

                case AT_SYMBOL:
                    if (isLetterOrDigit(ch)) {
                        state = State.DOMAIN;
                        domainLength = 1;
                        labelLength = 1;
                    } else {
                        return false;
                    }
                    break;

                case DOMAIN:
                    if (domainLength >= 255) return false; // Domain limit
                    if (ch == DOT) {
                        if (labelLength == 0 || address.charAt(i - 1) == HYPHEN) return false; // No empty labels, no hyphen-ended labels
                        state = State.DOT_DOMAIN;
                        labelLength = 0;
                        hasDotInDomain = true;
                    } else if (isLetterOrDigit(ch) || ch == HYPHEN) {
                        if (labelLength == 0 && ch == HYPHEN) return false; // No leading hyphen in a label
                        domainLength++;
                        labelLength++;
                    } else {
                        return false;
                    }
                    break;

                case DOT_DOMAIN:
                    if (isLetterOrDigit(ch)) {
                        state = State.DOMAIN;
                        domainLength++;
                        labelLength = 1;
                    } else {
                        return false;
                    }
                    break;
            }
        }

        // Ensure domain does not end with a hyphen or dot
        char lastChar = address.charAt(address.length() - 1);
        if (lastChar == DOT || lastChar == HYPHEN) return false;

        isLocalhost = address.endsWith("@localhost");
        return state == State.DOMAIN && hasAtSymbol && (hasDotInDomain || isLocalhost) && labelLength > 0 && domainLength <= 255;
    }

    private static boolean isLetterOrDigit(char ch) {
        return Character.isLetterOrDigit(ch);
    }

    private static boolean isValidLocalChar(char ch) {
        return Character.isLetterOrDigit(ch) || PRINTABLE_CHAR.indexOf(ch) != -1;
    }
}



