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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyntaxAnalysisModeTest {

    @Test
    public void testGetSyntaxAnalysisModeFromIntReturnsDoNotParseWhenIntIsZero() {
        assertEquals(SyntaxAnalysisMode.getSyntaxAnalysisModeFromInt(0), SyntaxAnalysisMode.DO_NOT_PARSE);
    }

    @Test
    public void testGetSyntaxAnalysisModeFromIntReturnsOnlyEqualLengthWhenIntIsOne() {
        assertEquals(SyntaxAnalysisMode.getSyntaxAnalysisModeFromInt(1), SyntaxAnalysisMode.ONLY_EQUAL_LENGTH);
    }

    @Test
    public void testGetSyntaxAnalysisModeFromIntReturnsEqualOrShorterLengthWhenIntIsTwo() {
        assertEquals(SyntaxAnalysisMode.getSyntaxAnalysisModeFromInt(2), SyntaxAnalysisMode.EQUAL_OR_SHORTER_LENGTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSyntaxAnalysisModeThrowsIllegalArgumentExceptionWhenGivenIntNotEqualToOneTwoOrThree() {
        SyntaxAnalysisMode.getSyntaxAnalysisModeFromInt(3);
    }
}
