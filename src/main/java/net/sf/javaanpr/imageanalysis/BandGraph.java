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

import net.sf.javaanpr.configurator.Configurator;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing of the horizontal projection of the detected region of the plate.
 * <p/>
 * The {@code peak} and {@code peakfoot} are detected on the X axis and multiplied with the {@code
 * peakDiffMultiplicationConstant}
 */
public class BandGraph extends Graph {

    private static final double peakFootConstant =
            Configurator.getConfigurator().getDoubleProperty("bandgraph_peakfootconstant"); // 0.75
    private static final double peakDiffMultiplicationConstant =
            Configurator.getConfigurator().getDoubleProperty("bandgraph_peakDiffMultiplicationConstant"); // 0.2

    /**
     * The Band to which this Graph is related.
     */
    private final Band handle;

    public BandGraph(Band handle) {
        this.handle = handle;
    }

    public List<Peak> findPeaks(int count) {
        List<Peak> outPeaks = new ArrayList<>();
        for (int c = 0; c < count; c++) {
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i = 0; i < yValues.size(); i++) { // left to right
                if (allowedInterval(outPeaks, i)) {
                    if (yValues.get(i) >= maxValue) {
                        maxValue = yValues.get(i);
                        maxIndex = i;
                    }
                }
            }
            // we found the biggest peak, let's do the first cut
            int leftIndex = indexOfLeftPeakRel(maxIndex, BandGraph.peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex, BandGraph.peakFootConstant);
            int diff = rightIndex - leftIndex;
            leftIndex -= BandGraph.peakDiffMultiplicationConstant * diff;
            rightIndex += BandGraph.peakDiffMultiplicationConstant * diff;
            outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(yValues.size() - 1, rightIndex)));
        }
        // filter the candidates that don't correspond with plate proportions
        List<Peak> outPeaksFiltered = new ArrayList<>();
        for (Peak p : outPeaks) {
            if ((p.getDiff() > (2 * handle.getHeight())) // plate too thin
                    && (p.getDiff() < (15 * handle.getHeight()))) { // plate too wide
                outPeaksFiltered.add(p);
            }
        }
        outPeaksFiltered.sort(new PeakComparator(yValues));
        super.peaks = outPeaksFiltered;
        return outPeaksFiltered;
    }

    public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
        int index = peak;
        for (int i = peak; i >= 0; i--) {
            index = i;
            if (yValues.get(index) < peakFootConstantAbs) {
                break;
            }
        }
        return Math.max(0, index);
    }

    public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
        int index = peak;
        for (int i = peak; i < yValues.size(); i++) {
            index = i;
            if (yValues.get(index) < peakFootConstantAbs) {
                break;
            }
        }
        return Math.min(yValues.size(), index);
    }
}
