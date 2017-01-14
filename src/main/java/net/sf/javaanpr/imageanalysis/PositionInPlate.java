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

package net.sf.javaanpr.imageanalysis;

/**
 * The coordinates of a character in the plate.
 */
public class PositionInPlate { // TODO refactor

    /**
     * Left X coordinate.
     */
    public final int x1;

    /**
     * Right X coordinate.
     */
    public final int x2;

    public PositionInPlate(int x1, int x2) {
        this.x1 = x1;
        this.x2 = x2;
    }
}
