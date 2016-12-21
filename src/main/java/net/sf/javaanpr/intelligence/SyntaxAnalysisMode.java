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

public enum SyntaxAnalysisMode {
    DO_NOT_PARSE,
    ONLY_EQUAL_LENGTH,
    EQUAL_OR_SHORTER_LENGTH;

    public static SyntaxAnalysisMode getSyntaxAnalysisModeFromInt(int syntaxAnalysisModeInt) {
        switch (syntaxAnalysisModeInt) {
            case 0:
                return SyntaxAnalysisMode.DO_NOT_PARSE;
            case 1:
                return SyntaxAnalysisMode.ONLY_EQUAL_LENGTH;
            case 2:
                return SyntaxAnalysisMode.EQUAL_OR_SHORTER_LENGTH;
            default:
                throw new IllegalArgumentException("Expected: 0, 1, or 2. Got: " + syntaxAnalysisModeInt);
        }
    }
}
