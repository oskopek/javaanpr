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
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package javaanpr.imageanalysis;

import java.awt.image.BufferedImage;

//import javaanpr.imageanalysis.Photo;

public class Statistics {
	public float maximum;
	public float minimum;
	public float average;
	public float dispersion;

	Statistics(BufferedImage bi) {
		this(new Photo(bi));
	}

	Statistics(Photo photo) {
		float sum = 0;
		float sum2 = 0;
		int w = photo.getWidth();
		int h = photo.getHeight();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float pixelValue = photo.getBrightness(x, y);
				maximum = Math.max(pixelValue, maximum);
				minimum = Math.min(pixelValue, minimum);
				sum += pixelValue;
				sum2 += (pixelValue * pixelValue);
			}
		}
		int count = (w * h);
		average = sum / count;
		// rozptyl = priemer stvorcov + stvorec priemeru
		dispersion = (sum2 / count) - (average * average);
	}

	public float thresholdBrightness(float value, float coef) {
		float out;
		if (value > average) {
			out = coef
					+ (((1 - coef) * (value - average)) / (maximum - average));
		} else {
			out = ((1 - coef) * (value - minimum)) / (average - minimum);
		}
		return out;
	}
}
