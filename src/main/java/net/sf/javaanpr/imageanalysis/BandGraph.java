/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://net.sf.javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://net.sf.javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package net.sf.javaanpr.imageanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import net.sf.javaanpr.intelligence.Intelligence;

public class BandGraph extends Graph {/* TODO - BEGIN */
	Band handle;

	private static double peakFootConstant = Intelligence.configurator
			.getDoubleProperty("bandgraph_peakfootconstant"); // 0.75
	private static double peakDiffMultiplicationConstant = Intelligence.configurator
			.getDoubleProperty("bandgraph_peakDiffMultiplicationConstant"); // 0.2

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

			return yValues.elementAt(((Peak) peak).getCenter()); // velkost
																	// peaku
		}

		@Override
		public int compare(Object peak1, Object peak2) {
			double comparison = getPeakValue(peak2) - getPeakValue(peak1);
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
		Vector<Graph.Peak> outPeaks = new Vector<Peak>();

		for (int c = 0; c < count; c++) { // for count
			float maxValue = 0.0f;
			int maxIndex = 0;
			for (int i = 0; i < yValues.size(); i++) { // zlava doprava
				if (allowedInterval(outPeaks, i)) { // ak potencialny vrchol sa
													// nachadza vo "volnom"
													// intervale, ktory nespada
													// pod ine vrcholy
					if (yValues.elementAt(i) >= maxValue) {
						maxValue = yValues.elementAt(i);
						maxIndex = i;
					}
				}
			} // end for int 0->max

			// nasli sme najvacsi peak // urobime 1. vysek
			int leftIndex = indexOfLeftPeakRel(maxIndex,
					BandGraph.peakFootConstant);
			int rightIndex = indexOfRightPeakRel(maxIndex,
					BandGraph.peakFootConstant);
			int diff = rightIndex - leftIndex;
			leftIndex -= BandGraph.peakDiffMultiplicationConstant * diff; /* CONSTANT */
			rightIndex += BandGraph.peakDiffMultiplicationConstant * diff; /* CONSTANT */

			outPeaks.add(new Peak(Math.max(0, leftIndex), maxIndex, Math.min(
					yValues.size() - 1, rightIndex)));
		} // end for count

		// treba filtrovat kandidatov, ktory nezodpovedaju proporciam znacky
		Vector<Peak> outPeaksFiltered = new Vector<Peak>();
		for (Peak p : outPeaks) {
			if ((p.getDiff() > (2 * handle.getHeight())) && // ak nieje znacka
					(// prilis uzka
					p.getDiff() < (15 * handle.getHeight() // alebo nie je
															// prilis siroka
					))) {
				outPeaksFiltered.add(p);// znacka ok, bereme ju
				// else outPeaksFiltered.add(p);// znacka ok, bereme ju
			}
		}

		Collections.sort(outPeaksFiltered, new PeakComparer(yValues));
		super.peaks = outPeaksFiltered;
		return outPeaksFiltered;

	}

	public int indexOfLeftPeakAbs(int peak, double peakFootConstantAbs) {
		int index = peak;
		//int counter = 0;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (yValues.elementAt(index) < peakFootConstantAbs) {
				break;
			}
		}
		return Math.max(0, index);
	}

	public int indexOfRightPeakAbs(int peak, double peakFootConstantAbs) {
		int index = peak;
		//int counter = 0;
		for (int i = peak; i < yValues.size(); i++) {
			index = i;
			if (yValues.elementAt(index) < peakFootConstantAbs) {
				break;
			}
		}
		return Math.min(yValues.size(), index);
	}
	/* TODO - END */
}
