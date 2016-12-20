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

import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.*;

public class CharacterRecognizerTest {
    private static final double epsilon = 5.96e-08;
    private RecognizedChar recognizedChar;

    @Before
    public void setup() {
        recognizedChar = new RecognizedChar();
        recognizedChar.addPattern(new RecognizedPattern('A', 3.0f));
        recognizedChar.addPattern(new RecognizedPattern('B', 1.0f));
        recognizedChar.addPattern(new RecognizedPattern('C', 4.0f));
    }

    @Test
    public void testPatternsCorrectlySortedAscending() {
        assertFalse(recognizedChar.isSorted());
        recognizedChar.sort(0);
        assertTrue(recognizedChar.isSorted());
        Vector<RecognizedPattern> patterns = recognizedChar.getPatterns();
        assertEquals(patterns.get(0).getCost(), 1.0f, epsilon);
        assertEquals(patterns.get(1).getCost(), 3.0f, epsilon);
        assertEquals(patterns.get(2).getCost(), 4.0f, epsilon);
    }

    @Test
    public void testPatternsCorrectlySortedDescending() {
        assertFalse(recognizedChar.isSorted());
        recognizedChar.sort(1);
        assertTrue(recognizedChar.isSorted());
        Vector<RecognizedPattern> patterns = recognizedChar.getPatterns();
        assertEquals(patterns.get(0).getCost(), 4.0f, epsilon);
        assertEquals(patterns.get(1).getCost(), 3.0f, epsilon);
        assertEquals(patterns.get(2).getCost(), 1.0f, epsilon);
    }

    @Test
    public void testGetPatternReturnsCorrectPatternWhenPatternsSorted() {
        recognizedChar.sort(0);
        assertEquals(recognizedChar.getPattern(2).getCost(), 4.0f, epsilon);
    }

    @Test
    public void testGetPatternReturnsNullWhenPatternsNotSorted() {
        assertNull(recognizedChar.getPattern(2));
    }
}
