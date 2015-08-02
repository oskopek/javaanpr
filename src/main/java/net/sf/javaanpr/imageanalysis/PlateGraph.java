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

public class PlateGraph extends Graph {

    Plate handle;

    private static double plategraph_rel_minpeaksize = Configurator.getConfigurator().getDoubleProperty(
            "plategraph_rel_minpeaksize");
    private static double peakFootConstant = Configurator.getConfigurator().getDoubleProperty(
            "plategraph_peakfootconstant");

    public PlateGraph(Plate handle) {
        this.handle = handle; // nesie odkaz na obrazok (Plate), ku ktoremu sa
        // graf vztahuje
    }

    public class SpaceComparer implements Comparator<Object> {
        Vector<Float> yValues = null;

        public SpaceComparer(Vector<Float> yValues) {
            this.yValues = yValues;
        }

        private float getPeakValue(Object peak) {
            return ((Peak) peak).getCenter(); // left > right
            // return this.yValues.elementAt( ((Peak)peak).center() );
        }

        @Override
        public int compare(Object peak1, Object peak2) {
            double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
            if (comparison < 0) {
                return 1;
            }
            if (comparison > 0) {
                return -1;
            }
            return 0;
        }
    }

    public Vector<Peak> findPeaks(int count) {
        Vector<Peak> spacesTemp = new Vector<Peak>();

        // uprava grafu pred segmentaciou :
        // 1. zistime average value a maxval
        // 2. upravime minval ako
        // diffVal = average - (maxval - average) = 2average - maxval
        // val -= diffVal

        float diffGVal = (2 * this.getAverageValue()) - this.getMaxValue();

        Vector<Float> yValuesNew = new Vector<Float>();
        for (Float f : this.yValues) {
            yValuesNew.add(f.floatValue() - diffGVal);
        }
        this.yValues = yValuesNew;

        this.deActualizeFlags();
        // end

        for (int c = 0; c < count; c++) { // for count
            float maxValue = 0.0f;
            int maxIndex = 0;
            for (int i = 0; i < this.yValues.size(); i++) { // zlava doprava
                if (this.allowedInterval(spacesTemp, i)) { // ak potencialny vrchol
                    // sa nachadza vo
                    // "volnom" intervale,
                    // ktory nespada pod ine
                    // vrcholy
                    if (this.yValues.elementAt(i) >= maxValue) {
                        maxValue = this.yValues.elementAt(i);
                        maxIndex = i;
                    }
                }
            } // end for int 0->max
            // nasli sme najvacsi peak
            // 0.75 mensie cislo znamena tendenciu znaky sekat, vacsie cislo
            // zase tendenciu nespravne zdruzovat
            if (this.yValues.elementAt(maxIndex) < (PlateGraph.plategraph_rel_minpeaksize * this.getMaxValue())) {
                break;
            }

            int leftIndex = this.indexOfLeftPeakRel(maxIndex, PlateGraph.peakFootConstant); // urci
            // sirku
            // detekovanej
            // medzery
            int rightIndex = this.indexOfRightPeakRel(maxIndex, PlateGraph.peakFootConstant);

            spacesTemp.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(this.yValues.size() - 1, rightIndex)));
        } // end for count

        // treba filtrovat kandidatov, ktory nezodpovedaju proporciam MEDZERY
        Vector<Peak> spaces = new Vector<Peak>();
        for (Peak p : spacesTemp) {
            if (p.getDiff() < (1 * this.handle.getHeight() // medzera nesmie byt
                    // siroka
            )) {
                spaces.add(p); // znacka ok, bereme ju
                // else outPeaksFiltered.add(p);// znacka ok, bereme ju
            }
        }

        // Vector<Peak> space OBSAHUJE MEDZERY, zoradime LEFT -> RIGHT
        Collections.sort(spaces, new SpaceComparer(this.yValues));

        // outPeaksFiltered teraz obsahuje MEDZERY ... v nasledujucom kode
        // ich transformujeme na pismena
        Vector<Peak> chars = new Vector<Peak>();

        /*
         * + + +++ +++ + + +++ + + + + + + + + + + + ++ + + + ++ +++ +++ | | 1 | 2 .... | +--> 1. local minimum
         */

        // zapocitame aj znak od medzery na lavo :
        if (spaces.size() != 0) {
            // detekujeme 1. lokalne minimum na grafe
            // 3 = leftmargin
            // int minIndex = getMinValueIndex(0, spaces.elementAt(0).getCenter());
            // System.out.println("minindex found at " + minIndex +
            // " in interval 0 - " + outPeaksFiltered.elementAt(0).getCenter());
            // hladame index do lava od minindex
            int leftIndex = 0;
            // for (int i=minIndex; i>=0; i--) {
            // leftIndex = i;
            // if (this.yValues.elementAt(i) >
            // 0.9 * this.yValues.elementAt(
            // outPeaksFiltered.elementAt(0).getCenter()
            // )
            // ) break;
            // }

            Peak first = new Peak(leftIndex/* 0 */, spaces.elementAt(0).getCenter());
            if (first.getDiff() > 0) {
                chars.add(first);
            }
        }

        for (int i = 0; i < (spaces.size() - 1); i++) {
            int left = spaces.elementAt(i).getCenter();
            int right = spaces.elementAt(i + 1).getCenter();
            chars.add(new Peak(left, right));
        }

        // znak ktory je napravo od poslednej medzery :
        if (spaces.size() != 0) {
            Peak last = new Peak(spaces.elementAt(spaces.size() - 1).getCenter(), this.yValues.size() - 1);
            if (last.getDiff() > 0) {
                chars.add(last);
            }
        }

        super.peaks = chars;
        return chars;

    }
    // public int indexOfLeftPeak(int peak) {
    // int index=peak;
    // int counter = 0;
    // for (int i=peak; i>=0; i--) {
    // index = i;
    // if (yValues.elementAt(index) < 0.7 * yValues.elementAt(peak) ) break;
    // }
    // return Math.max(0,index);
    // }
    // public int indexOfRightPeak(int peak) {
    // int index=peak;
    // int counter = 0;
    // for (int i=peak; i<yValues.size(); i++) {
    // index = i;
    // if (yValues.elementAt(index) < 0.7 * yValues.elementAt(peak) ) break;
    // }
    // return Math.min(yValues.size(), index);
    // }

    // public float minValInInterval(float a, float b) {
    // int ia = (int)(a*yValues.size());
    // int ib = (int)(b*yValues.size());
    // float min = Float.POSITIVE_INFINITY;
    // for (int i=ia; i<ib;i++) {
    // min = Math.min(min, yValues.elementAt(i));
    // }
    // return min;
    // }
    // public float maxValInInterval(float a, float b) {
    // int ia = (int)(a*yValues.size());
    // int ib = (int)(b*yValues.size());
    // float max = 0;
    // for (int i=ia; i<ib;i++) {
    // max = Math.max(max, yValues.elementAt(i));
    // }
    // return max;
    // }

}
