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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatternComparatorTest {

    @Test
    public void testCompareAscendingPatternsEqualReturnsZero() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 1.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('B', 1.0f);
        assertEquals(new PatternComparator(false).compare(recognizedPattern1, recognizedPattern2), 0);
    }

    @Test
    public void testCompareAscendingFirstPatternSmallerReturnsMinusOne() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 1.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('A', 2.0f);
        int comparisonResult = new PatternComparator(false).compare(recognizedPattern1, recognizedPattern2);
        assertTrue("Expected comparison result " + comparisonResult + " to be less than 0", comparisonResult < 0);
    }

    @Test
    public void testCompareAscendingFirstPatternLargerReturnsPlusOne() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 2.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('A', 1.0f);
        int comparisonResult = new PatternComparator(false).compare(recognizedPattern1, recognizedPattern2);
        assertTrue("Expected comparison result " + comparisonResult + " to be greater than 0", comparisonResult > 0);
    }

    @Test
    public void testCompareDescendingPatternsEqualReturnsZero() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 1.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('B', 1.0f);
        assertEquals(new PatternComparator(true).compare(recognizedPattern1, recognizedPattern2), 0);
    }

    @Test
    public void testCompareDescendingFirstPatternSmallerReturnsPlusOne() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 1.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('A', 2.0f);
        int comparisonResult = new PatternComparator(true).compare(recognizedPattern1, recognizedPattern2);
        assertTrue("Expected comparison result " + comparisonResult + " to be greater than 0", comparisonResult > 0);
    }

    @Test
    public void testCompareDescendingFirstPatternLargerReturnsMinusOne() {
        RecognizedPattern recognizedPattern1 = new RecognizedPattern('A', 2.0f);
        RecognizedPattern recognizedPattern2 = new RecognizedPattern('A', 1.0f);
        int comparisonResult = new PatternComparator(true).compare(recognizedPattern1, recognizedPattern2);
        assertTrue("Expected comparison result " + comparisonResult + " to be less than 0", comparisonResult < 0);
    }
}
