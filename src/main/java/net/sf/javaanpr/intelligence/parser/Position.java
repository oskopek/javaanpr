package net.sf.javaanpr.intelligence.parser;

public class Position {

    public final char[] allowedChars;

    public Position(String data) {
        allowedChars = data.toCharArray();
    }

    public boolean isAllowed(char chr) {
        boolean ret = false;
        for (char allowedChar : allowedChars) {
            if (allowedChar == chr) {
                ret = true;
            }
        }
        return ret;
    }

}
