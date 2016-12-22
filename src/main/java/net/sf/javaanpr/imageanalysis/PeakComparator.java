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

public class PeakComparator implements Comparator<Peak> {
    private PlateVerticalGraph graphHandle = null;

    public PeakComparator(PlateVerticalGraph graph) {
        this.graphHandle = graph;
    }

    private float getPeakValue(Peak peak) {
        // heuristic: how high (wide on the graph) is the candidate character (prefer higher ones)
        // return ((Peak)peak).getDiff();

        // heuristic: height of the peak
        return this.graphHandle.yValues.elementAt(peak.getCenter());

        // heuristic: how far from the center is the candidate
        // int peakCenter = ( ((Peak)peak).getRight() + ((Peak)peak).getLeft() )/2;
        // return Math.abs(peakCenter - this.graphHandle.yValues.size()/2);
    }

    @Override
    public int compare(Peak peak1, Peak peak2) {
        return Double.compare(getPeakValue(peak1), getPeakValue(peak2));
    }
}
