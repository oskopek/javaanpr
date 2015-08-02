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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class PlateVerticalGraph extends Graph {
    private static double peakFootConstant = // 0.42; /* CONSTANT*/
            Configurator.getConfigurator().getDoubleProperty("plateverticalgraph_peakfootconstant");

    Plate handle;

    public PlateVerticalGraph(Plate handle) {
        this.handle = handle;
    }

    public class PeakComparer implements Comparator<Object> {
        PlateVerticalGraph graphHandle = null;

        public PeakComparer(PlateVerticalGraph graph) {
            this.graphHandle = graph;
        }

        private float getPeakValue(Object peak) {
            // heuristika : aky vysoky (siroky na gragfe) je kandidat na pismeno
            // preferuju sa vyssie
            // return ((Peak)peak).getDiff();

            // vyska peaku
            return this.graphHandle.yValues.elementAt(((Peak) peak).getCenter());

            // heuristika :
            // ako daleko od stredu je kandidat
            // int peakCenter = ( ((Peak)peak).getRight() +
            // ((Peak)peak).getLeft() )/2;
            // return Math.abs(peakCenter - this.graphHandle.yValues.size()/2);
        }

        @Override
        public int compare(Object peak1, Object peak2) { // Peak
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) {
                return -1;
            }
            if (comparison > 0) {
                return 1;
            }
            return 0;
        }
    }

    public Vector<Peak> findPeak(int count) {

        // znizime peak
        for (int i = 0; i < this.yValues.size(); i++) {
            this.yValues.set(i, this.yValues.elementAt(i) - this.getMinValue());
        }

        Vector<Peak> outPeaks = new Vector<Peak>();

        for (int c = 0; c < count; c++) { // for count
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i = 0; i < this.yValues.size(); i++) { // zlava doprava
                if (this.allowedInterval(outPeaks, i)) { // ak potencialny vrchol sa
                    // nachadza vo "volnom"
                    // intervale, ktory nespada
                    // pod ine vrcholy
                    if (this.yValues.elementAt(i) >= maxValue) {
                        maxValue = this.yValues.elementAt(i);
                        maxIndex = i;
                    }
                }
            } // end for int 0->max
            // nasli sme najvacsi peak

            if (this.yValues.elementAt(maxIndex) < (0.05 * super.getMaxValue())) {
                break; // 0.4
            }

            int leftIndex = this.indexOfLeftPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);
            int rightIndex = this.indexOfRightPeakRel(maxIndex, PlateVerticalGraph.peakFootConstant);

            outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
        }

        Collections.sort(outPeaks, new PeakComparer(this));
        super.peaks = outPeaks;
        return outPeaks;
    }

}
