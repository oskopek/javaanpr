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

public class BandGraph extends Graph {
    private Band handle;

    private static double peakFootConstant = Configurator.getConfigurator().getDoubleProperty(
            "bandgraph_peakfootconstant"); // 0.75
    private static double peakDiffMultiplicationConstant = Configurator.getConfigurator().getDoubleProperty(
            "bandgraph_peakDiffMultiplicationConstant"); // 0.2

    public BandGraph(Band handle) {
        this.handle = handle; // nesie odkaz na obrazok (band), ku ktoremu sa
        // graf vztahuje
    }

    public class PeakComparer implements Comparator<Object> {
        Vector<Float> yValues = null;

        public PeakComparer(Vector<Float> yValues) {
            this.yValues = yValues;
        }

        private float getPeakValue(Object peak) {
            // return ((Peak)peak).center(); // left > right

            return this.yValues.elementAt(((Peak) peak).getCenter()); // velkost
            // peaku
        }

        @Override
        public int compare(Object peak1, Object peak2) {
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

    public Vector<Peak> findPeaks(int count) {
        Vector<Graph.Peak> outPeaks = new Vector<Graph.Peak>();

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

            // nasli sme najvacsi peak // urobime 1. vysek
            int leftIndex = this.indexOfLeftPeakRel(maxIndex, BandGraph.peakFootConstant);
            int rightIndex = this.indexOfRightPeakRel(maxIndex, BandGraph.peakFootConstant);
            int diff = rightIndex - leftIndex;
            leftIndex -= BandGraph.peakDiffMultiplicationConstant * diff; /* CONSTANT */
            rightIndex += BandGraph.peakDiffMultiplicationConstant * diff; /* CONSTANT */

            outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
        } // end for count

        // treba filtrovat kandidatov, ktory nezodpovedaju proporciam znacky
        Vector<Peak> outPeaksFiltered = new Vector<Peak>();
        for (Peak p : outPeaks) {
            if ((p.getDiff() > (2 * this.handle.getHeight())) && // ak nieje znacka
                    (// prilis uzka
                            p.getDiff() < (15 * this.handle.getHeight() // alebo nie je
                                    // prilis siroka
                            ))) {
                outPeaksFiltered.add(p); // znacka ok, bereme ju
                // else outPeaksFiltered.add(p);// znacka ok, bereme ju
            }
        }

        Collections.sort(outPeaksFiltered, new PeakComparer(this.yValues));
        super.peaks = outPeaksFiltered;
        return outPeaksFiltered;

    }

    public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
        int index = peak;
        // int counter = 0;
        for (int i = peak; i >= 0; i--) {
            index = i;
            if (this.yValues.elementAt(index) < peakFootConstantAbs) {
                break;
            }
        }
        return Math.max(0, index);
    }

    public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
        int index = peak;
        // int counter = 0;
        for (int i = peak; i < this.yValues.size(); i++) {
            index = i;
            if (this.yValues.elementAt(index) < peakFootConstantAbs) {
                break;
            }
        }
        return Math.min(this.yValues.size(), index);
    }
}
