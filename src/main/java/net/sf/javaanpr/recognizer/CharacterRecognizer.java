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

package net.sf.javaanpr.recognizer;

import net.sf.javaanpr.imageanalysis.Char;

public abstract class CharacterRecognizer {

    public static final char[] ALPHABET =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
                    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static final float[][] FEATURES = {{0, 1, 0, 1}, // 0
            {1, 0, 1, 0}, // 1
            {0, 0, 1, 1}, // 2
            {1, 1, 0, 0}, // 3
            {0, 0, 0, 1}, // 4
            {1, 0, 0, 0}, // 5
            {1, 1, 1, 0}, // 6
            {0, 1, 1, 1}, // 7
            {0, 0, 1, 0}, // 8
            {0, 1, 0, 0}, // 9
            {1, 0, 1, 1}, // 10
            {1, 1, 0, 1} // 11
    };

    public abstract RecognizedChar recognize(Char chr);
}
