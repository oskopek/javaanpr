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

import java.util.Comparator;
import java.util.List;

public class PeakComparator implements Comparator<Peak> {
    private final List<Float> yValues;

    public PeakComparator(List<Float> yValues) {
        this.yValues = yValues;
    }

    private float getPeakValue(Peak peak) {
        // heuristic: how high (wide on the graph) is the candidate character (prefer higher ones)
        // return peak.getDiff();

        // heuristic: height of the peak
        return yValues.get(peak.getCenter());

        // heuristic: how far from the center is the candidate
        // int peakCenter = (peak.getRight() + (peak.getLeft() )/2;
        // return Math.abs(peakCenter - yValues.size()/2);
    }

    @Override
    public int compare(Peak peak1, Peak peak2) {
        return Double.compare(getPeakValue(peak2), getPeakValue(peak1));
    }
}
