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

import java.util.Vector;

/**
 * This class represents a number plate being recognized. As each character (represented by {@link RecognizedChar})
 * is recognized it gets added to the {@code RecognizedPlate}.
 */
public class RecognizedPlate {

    private Vector<RecognizedChar> chars;

    /**
     * Constructs a new {@code RecognizedPlate} with no {@code RecognizedChar}s
     */
    public RecognizedPlate() {
        this.chars = new Vector<RecognizedChar>();
    }

    /**
     * Adds one instance of {@link RecognizedChar} to the list of {@code RecognizedChar}s which have been
     * recognized for this plate.
     * @param chr The new {@link RecognizedChar} to be added
     */
    public void addChar(RecognizedChar chr) {
        this.chars.add(chr);
    }

    /**
     * This method is for getting a specific {@link RecognizedChar} which has been added to this plate.
     * @param i The index of the {@link RecognizedChar} to be returned
     * @return The {@link RecognizedChar} which was the {@code i}th one to be added to this plate (starting from 0)
     * @throws ArrayIndexOutOfBoundsException if {@code i} is larger than the number of {@code RecognizedChar}s added
     * - 1 or {@code i} is less than 0.
     */
    public RecognizedChar getChar(int i) {
        return this.chars.elementAt(i);
    }

    /**
     * This method is for getting a string representation of all the {@code RecognizedChar}s added to this plate.
     * @return A string is made up of the character stored in the first pattern of each {@link RecognizedChar} with
     * a space in between each one.
     */
    public String getString() {
        String ret = new String("");
        for (int i = 0; i < this.chars.size(); i++) {
            ret = ret + this.chars.elementAt(i).getPattern(0).getChar();
        }
        return ret;
    }

    /**
     * @return All the {@code RecognizedChar}s added to this plate
     */
    public Vector<RecognizedChar> getChars() {
        return chars;
    }
}
