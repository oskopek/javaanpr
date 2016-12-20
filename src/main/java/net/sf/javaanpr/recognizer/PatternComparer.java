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

import java.util.Comparator;

public class PatternComparer implements Comparator<Object> {
    private int direction;

    public PatternComparer(int direction) {
        this.direction = direction;
    }

    @Override
    public int compare(Object o1, Object o2) {
        float cost1 = ((RecognizedPattern) o1).getCost();
        float cost2 = ((RecognizedPattern) o2).getCost();
        int ret = 0;
        if (cost1 < cost2) {
            ret = -1;
        }
        if (cost1 > cost2) {
            ret = 1;
        }
        if (this.direction == 1) {
            ret *= -1;
        }
        return ret;
    }
}
