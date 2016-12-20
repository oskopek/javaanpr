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

public class RecognizedPlate {

    private Vector<RecognizedChar> chars;

    public RecognizedPlate() {
        this.chars = new Vector<RecognizedChar>();
    }

    public void addChar(RecognizedChar chr) {
        this.chars.add(chr);
    }

    public RecognizedChar getChar(int i) {
        return this.chars.elementAt(i);
    }

    public String getString() {
        String ret = new String("");
        for (int i = 0; i < this.chars.size(); i++) {
            ret = ret + this.chars.elementAt(i).getPattern(0).getChar();
        }
        return ret;
    }

    public Vector<RecognizedChar> getChars() {
        return chars;
    }
}
