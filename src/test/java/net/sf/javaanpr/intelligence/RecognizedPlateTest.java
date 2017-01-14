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

package net.sf.javaanpr.intelligence;

import net.sf.javaanpr.recognizer.RecognizedChar;
import net.sf.javaanpr.recognizer.RecognizedPattern;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecognizedPlateTest {

    private RecognizedPlate getRecognizedPlateWithThreeRecognizedChars() {
        RecognizedPlate recognizedPlate = new RecognizedPlate();
        RecognizedChar recognizedChar1 = new RecognizedChar();
        recognizedChar1.addPattern(new RecognizedPattern('A', 1.0f));
        recognizedChar1.sort(false);
        RecognizedChar recognizedChar2 = new RecognizedChar();
        recognizedChar2.addPattern(new RecognizedPattern('B', 2.0f));
        recognizedChar2.addPattern(new RecognizedPattern('C', 3.0f));
        recognizedChar2.sort(false);
        RecognizedChar recognizedChar3 = new RecognizedChar();
        recognizedChar3.addPattern(new RecognizedPattern('D', 4.0f));
        recognizedChar3.sort(false);
        recognizedPlate.addChar(recognizedChar1);
        recognizedPlate.addChar(recognizedChar2);
        recognizedPlate.addChar(recognizedChar3);
        return recognizedPlate;
    }

    @Test
    public void testCanAddAndGetChars() {
        RecognizedPlate recognizedPlate = getRecognizedPlateWithThreeRecognizedChars();
        assertEquals(recognizedPlate.getChar(0).getPattern(0).getChar(), 'A');
        assertEquals(recognizedPlate.getChar(1).getPattern(0).getChar(), 'B');
        assertEquals(recognizedPlate.getChar(1).getPattern(1).getChar(), 'C');
        assertEquals(recognizedPlate.getChar(2).getPattern(0).getChar(), 'D');
    }

    @Test
    public void testCanAddAndGetAllChars() {
        RecognizedPlate recognizedPlate = getRecognizedPlateWithThreeRecognizedChars();
        List<RecognizedChar> recognizedChars = recognizedPlate.getChars();
        assertEquals(recognizedChars.get(0).getPattern(0).getChar(), 'A');
        assertEquals(recognizedChars.get(1).getPattern(0).getChar(), 'B');
        assertEquals(recognizedChars.get(1).getPattern(1).getChar(), 'C');
        assertEquals(recognizedChars.get(2).getPattern(0).getChar(), 'D');
    }

    @Test
    public void testCanGetStringFromChars() {
        RecognizedPlate recognizedPlate = getRecognizedPlateWithThreeRecognizedChars();
        assertEquals(recognizedPlate.getString(), "ABD");
    }
}
