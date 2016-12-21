/*
 * Copyright 2013 JavaANPR contributors
 * Copyright 2006 Ondrej Martinsky
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package net.sf.javaanpr.test.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class which helps in having methods for Testing.
 */
public class TestUtility {
    public static final double epsilon = 5.96e-08;

    public StringBuilder readFile(final String filename) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        final StringBuilder sb = new StringBuilder();
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            sb.append(currentLine);
        }
        return sb;
    }

}
