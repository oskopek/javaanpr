package net.sf.javaanpr.intelligence.parser;

public class Position {

    public final char[] allowedChars;

    public Position(String data) {
        allowedChars = data.toCharArray();
    }

    public boolean isAllowed(char chr) {
        boolean ret = false;
        for (int i = 0; i < allowedChars.length; i++) {
            if (allowedChars[i] == chr) {
                ret = true;
            }
        }
        return ret;
    }

}
