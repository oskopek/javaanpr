package net.sf.javaanpr.test.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class which helps in having methods for Testing.
 */
public class TestUtility {

    public StringBuilder readFile(final String filename) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        final StringBuilder sb = new StringBuilder();
        String currentLine = null;
        while ((currentLine = br.readLine()) != null) {
            sb.append(currentLine);
        }
        return sb;
    }

}
