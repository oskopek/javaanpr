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

//import java.util.Collections;
//import java.util.Comparator;
import java.util.Vector;

import net.sf.javaanpr.intelligence.Intelligence;

public class PlateHorizontalGraph extends Graph {
	/*private static double peakFootConstant = // 0.1; //CONSTANT
	Intelligence.configurator
			.getDoubleProperty("platehorizontalgraph_peakfootconstant");
	*/
	
	private static int horizontalDetectionType = Intelligence.configurator
			.getIntProperty("platehorizontalgraph_detectionType");
	
	Plate handle;

	public PlateHorizontalGraph(Plate handle) {
		this.handle = handle;
	}

	public float derivation(int index1, int index2) {
		return yValues.elementAt(index1) - yValues.elementAt(index2);
	}

	public Vector<Peak> findPeak(int count) {
		if (PlateHorizontalGraph.horizontalDetectionType == 1) {
			return findPeak_edgedetection(count);
		}
		return findPeak_derivate(count);
	}

	public Vector<Peak> findPeak_derivate(int count) { // RIESENIE DERIVACIOU
		int a, b;
		float maxVal = this.getMaxValue();

		for (a = 2; (-derivation(a, a + 4) < (maxVal * 0.2))
				&& (a < (yValues.size() - 2 - 2 - 4)); a++) {
			;
		}
		for (b = yValues.size() - 1 - 2; (derivation(b - 4, b) < (maxVal * 0.2))
				&& (b > (a + 2)); b--) {
			;
		}

		Vector<Peak> outPeaks = new Vector<Peak>();

		outPeaks.add(new Peak(a, b));
		super.peaks = outPeaks;
		return outPeaks;
	}

	public Vector<Peak> findPeak_edgedetection(int count) {
		float average = this.getAverageValue();
		int a, b;
		for (a = 0; yValues.elementAt(a) < average; a++) {
			;
		}
		for (b = yValues.size() - 1; yValues.elementAt(b) < average; b--) {
			;
		}

		Vector<Peak> outPeaks = new Vector<Peak>();
		a = Math.max(a - 5, 0);
		b = Math.min(b + 5, yValues.size());

		outPeaks.add(new Peak(a, b));
		super.peaks = outPeaks;
		return outPeaks;
	}
}
