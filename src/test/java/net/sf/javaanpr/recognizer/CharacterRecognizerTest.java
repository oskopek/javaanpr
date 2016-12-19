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

import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class CharacterRecognizerTest {

    private CharacterRecognizer.RecognizedChar getRecognizedCharWithThreePatterns() {
        CharacterRecognizer.RecognizedChar recognizedChar = new CharacterRecognizer.RecognizedChar();
        recognizedChar.addPattern(new CharacterRecognizer.RecognizedChar.RecognizedPattern('A', 3.0f));
        recognizedChar.addPattern(new CharacterRecognizer.RecognizedChar.RecognizedPattern('B', 1.0f));
        recognizedChar.addPattern(new CharacterRecognizer.RecognizedChar.RecognizedPattern('C', 4.0f));
        return recognizedChar;
    }

    @Test
    public void testPatternsCorrectlySortedAscending() {
        CharacterRecognizer.RecognizedChar recognizedChar = getRecognizedCharWithThreePatterns();
        assertFalse(recognizedChar.isSorted());
        recognizedChar.sort(0);
        assertTrue(recognizedChar.isSorted());
        Vector<CharacterRecognizer.RecognizedChar.RecognizedPattern> patterns = recognizedChar.getPatterns();
        assertThat(patterns.get(0).getCost(), is(1.0f));
        assertThat(patterns.get(1).getCost(), is(3.0f));
        assertThat(patterns.get(2).getCost(), is(4.0f));
    }

    @Test
    public void testPatternsCorrectlySortedDescending() {
        CharacterRecognizer.RecognizedChar recognizedChar = getRecognizedCharWithThreePatterns();
        assertFalse(recognizedChar.isSorted());
        recognizedChar.sort(1);
        assertTrue(recognizedChar.isSorted());
        Vector<CharacterRecognizer.RecognizedChar.RecognizedPattern> patterns = recognizedChar.getPatterns();
        assertThat(patterns.get(0).getCost(), is(4.0f));
        assertThat(patterns.get(1).getCost(), is(3.0f));
        assertThat(patterns.get(2).getCost(), is(1.0f));
    }

    @Test
    public void testGetPatternReturnsCorrectPatternWhenPatternsSorted() {
        CharacterRecognizer.RecognizedChar recognizedChar = getRecognizedCharWithThreePatterns();
        recognizedChar.sort(0);
        assertThat(recognizedChar.getPattern(2).getCost(), is(4.0f));
    }

    @Test
    public void testGetPatternReturnsNullWhenPatternsNotSorted() {
        CharacterRecognizer.RecognizedChar recognizedChar = getRecognizedCharWithThreePatterns();
        assertNull(recognizedChar.getPattern(2));
    }
}
