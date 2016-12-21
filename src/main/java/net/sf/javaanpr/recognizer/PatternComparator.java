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

public class PatternComparator implements Comparator<RecognizedPattern> {
    private boolean shouldSortDescending;

    public PatternComparator(boolean shouldSortDescending) {
        this.shouldSortDescending = shouldSortDescending;
    }

    @Override
    public int compare(RecognizedPattern recognizedPattern1, RecognizedPattern recognizedPattern2) {
        float cost1 = recognizedPattern1.getCost();
        float cost2 = recognizedPattern2.getCost();
        int ret = 0;
        if (cost1 < cost2) {
            ret = -1;
        }
        if (cost1 > cost2) {
            ret = 1;
        }
        if (shouldSortDescending) {
            ret *= -1;
        }
        return ret;
    }
}
