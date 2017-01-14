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

public class PlateVerticalGraph extends Graph {

    private static final double peakFootConstant =
            Configurator.getConfigurator().getDoubleProperty("plateverticalgraph_peakfootconstant"); // 0.42

    public List<Peak> findPeak(int count) {
        // lower the peak
        for (int i = 0; i < yValues.size(); i++) {
            yValues.set(i, yValues.get(i) - getMinValue());
        }
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
            // we found the biggest peak
            if (yValues.get(maxIndex) < (0.05 * super.getMaxValue())) {
                break; // 0.4
            }
            int leftIndex = indexOfLeftPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);
            int rightIndex = indexOfRightPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);
            outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(yValues.size() - 1, rightIndex)));
        }
        outPeaks.sort(new PeakComparator(yValues));
        super.peaks = outPeaks;
        return outPeaks;
    }
}
