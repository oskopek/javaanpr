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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.Vector;

public class Graph {
	public class Peak {
		public int left, center, right;

		public Peak(int left, int center, int right) {
			this.left = left;
			this.center = center;
			this.right = right;
		}

		public Peak(int left, int right) {
			this.left = left;
			center = (left + right) / 2;
			this.right = right;
		}

		public int getLeft() {
			return left;
		}

		public int getRight() {
			return right;
		}

		public int getCenter() {
			return center;
		}

		public int getDiff() {
			return right - left;
		}

		public void setLeft(int left) {
			this.left = left;
		}

		public void setCenter(int center) {
			this.center = center;
		}

		public void setRight(int right) {
			this.right = right;
		}
	}

	static public class ProbabilityDistributor {
		float center;
		float power;
		int leftMargin;
		int rightMargin;

		public ProbabilityDistributor(float center, float power,
				int leftMargin, int rightMargin) {
			this.center = center;
			this.power = power;
			this.leftMargin = Math.max(1, leftMargin);
			this.rightMargin = Math.max(1, rightMargin);
		}

		private float distributionFunction(float value, float positionPercentage) {
			return value
					* (1 - (power * Math.abs(positionPercentage - center)));
		}

		public Vector<Float> distribute(Vector<Float> peaks) {
			Vector<Float> distributedPeaks = new Vector<Float>();
			for (int i = 0; i < peaks.size(); i++) {
				if ((i < leftMargin) || (i > (peaks.size() - rightMargin))) {
					distributedPeaks.add(0f);
				} else {
					distributedPeaks.add(distributionFunction(
							peaks.elementAt(i), ((float) i / peaks.size())));
				}
			}

			return distributedPeaks;
		}
	}

	public Vector<Peak> peaks = null;
	public Vector<Float> yValues = new Vector<Float>();
	// statistical informations
	private boolean actualAverageValue = false; // su hodnoty aktualne ?
	private boolean actualMaximumValue = false; // su hodnoty aktualne ?
	private boolean actualMinimumValue = false; // su hodnoty aktualne ?
	private float averageValue;
	private float maximumValue;
	private float minimumValue;

	void deActualizeFlags() {
		actualAverageValue = false;
		actualMaximumValue = false;
		actualMinimumValue = false;
	}

	// generic
	// methods for searching bands in image !
	boolean allowedInterval(Vector<Peak> peaks, int xPosition) {
		for (Peak peak : peaks) {
			if ((peak.left <= xPosition) && (xPosition <= peak.right)) {
				return false;
			}
		}
		return true;
	}

	public void addPeak(float value) {
		yValues.add(value);
		deActualizeFlags();
	}

	public void applyProbabilityDistributor(
			Graph.ProbabilityDistributor probability) {
		yValues = probability.distribute(yValues);
		deActualizeFlags();
	}

	public void negate() {
		float max = this.getMaxValue();
		for (int i = 0; i < yValues.size(); i++) {
			yValues.setElementAt(max - yValues.elementAt(i), i);
		}

		deActualizeFlags();
	}

	// public class PeakComparer implements Comparator {
	// int sortBy; // 0 = podla sirky, 1 = podla velkosti, 2 = z lava do prava
	// Vector<Float> yValues = null;
	//
	// public PeakComparer(Vector<Float> yValues, int sortBy) {
	// this.yValues = yValues;
	// this.sortBy = sortBy;
	// }
	//
	// private float getPeakValue(Object peak) {
	// if (this.sortBy == 0) {
	// return ((Peak)peak).diff();
	// } else if (this.sortBy == 1) {
	// return this.yValues.elementAt( ((Peak)peak).center() );
	// } else if (this.sortBy == 2) {
	// return ((Peak)peak).center();
	// }
	// return 0;
	// }
	//
	// public int compare(Object peak1, Object peak2) { // Peak
	// double comparison = this.getPeakValue(peak2) - this.getPeakValue(peak1);
	// if (comparison < 0) return -1;
	// if (comparison > 0) return 1;
	// return 0;
	// }
	// }

	// float getAverageValue() {
	// if (!this.actualAverageValue) {
	// float sum = 0.0f;
	// for (Float peak : this.yValues) sum += peak;
	// this.averageValue = sum/this.yValues.size();
	// this.actualAverageValue = true;
	// }
	// return this.averageValue;
	// }
	//

	float getAverageValue() {
		if (!actualAverageValue) {
			averageValue = getAverageValue(0, yValues.size());
			actualAverageValue = true;
		}
		return averageValue;
	}

	float getAverageValue(int a, int b) {
		float sum = 0.0f;
		for (int i = a; i < b; i++) {
			sum += yValues.elementAt(i).doubleValue();
		}
		return sum / yValues.size();
	}

	// float getMaxValue() {
	// if (!this.actualMaximumValue) {
	// float maxValue = 0.0f;
	// for (int i=0; i<yValues.size(); i++)
	// maxValue = Math.max(maxValue, yValues.elementAt(i));
	// this.maximumValue = maxValue;
	// this.actualMaximumValue = true;
	// }
	// return this.maximumValue;
	// }

	float getMaxValue() {
		if (!actualMaximumValue) {
			maximumValue = this.getMaxValue(0, yValues.size());
			actualMaximumValue = true;
		}
		return maximumValue;
	}

	float getMaxValue(int a, int b) {
		float maxValue = 0.0f;
		for (int i = a; i < b; i++) {
			maxValue = Math.max(maxValue, yValues.elementAt(i));
		}
		return maxValue;
	}

	float getMaxValue(float a, float b) {
		int ia = (int) (a * yValues.size());
		int ib = (int) (b * yValues.size());
		return getMaxValue(ia, ib);
	}

	int getMaxValueIndex(int a, int b) {
		float maxValue = 0.0f;
		int maxIndex = a;
		for (int i = a; i < b; i++) {
			if (yValues.elementAt(i) >= maxValue) {
				maxValue = yValues.elementAt(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	// float getMinValue() {
	// if (!this.actualMinimumValue) {
	// float minValue = Float.POSITIVE_INFINITY;
	// for (int i=0; i<yValues.size(); i++)
	// minValue = Math.min(minValue, yValues.elementAt(i));
	//
	// this.minimumValue = minValue;
	// this.actualMinimumValue = true;
	// }
	// return this.minimumValue;
	// }

	float getMinValue() {
		if (!actualMinimumValue) {
			minimumValue = this.getMinValue(0, yValues.size());
			actualMinimumValue = true;
		}
		return minimumValue;
	}

	float getMinValue(int a, int b) {
		float minValue = Float.POSITIVE_INFINITY;
		for (int i = a; i < b; i++) {
			minValue = Math.min(minValue, yValues.elementAt(i));
		}
		return minValue;
	}

	float getMinValue(float a, float b) {
		int ia = (int) (a * yValues.size());
		int ib = (int) (b * yValues.size());
		return getMinValue(ia, ib);
	}

	int getMinValueIndex(int a, int b) {
		float minValue = Float.POSITIVE_INFINITY;
		int minIndex = b;
		for (int i = a; i < b; i++) {
			if (yValues.elementAt(i) <= minValue) {
				minValue = yValues.elementAt(i);
				minIndex = i;
			}
		}
		return minIndex;
	}

	//

	public BufferedImage renderHorizontally(int width, int height) {
		BufferedImage content = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		BufferedImage axis = new BufferedImage(width + 40, height + 40,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D graphicContent = content.createGraphics();
		Graphics2D graphicAxis = axis.createGraphics();

		Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
		graphicAxis.setColor(Color.LIGHT_GRAY);
		graphicAxis.fill(backRect);
		graphicAxis.draw(backRect);
		backRect = new Rectangle(0, 0, width, height);
		graphicContent.setColor(Color.WHITE);
		graphicContent.fill(backRect);
		graphicContent.draw(backRect);

		int x, y, x0, y0;
		x = 0;
		y = 0;

		graphicContent.setColor(Color.GREEN);

		for (int i = 0; i < yValues.size(); i++) {
			x0 = x;
			y0 = y;
			x = (int) (((float) i / yValues.size()) * width);
			y = (int) ((1 - (yValues.elementAt(i) / this.getMaxValue())) * height);
			graphicContent.drawLine(x0, y0, x, y);
		}

		if (peaks != null) { // uz boli vyhladane aj peaky, renderujeme aj
								// tie
			graphicContent.setColor(Color.RED);
			int i = 0;
			double multConst = (double) width / yValues.size();
			for (Peak p : peaks) {
				graphicContent.drawLine((int) (p.left * multConst), 0,
						(int) (p.center * multConst), 30);
				graphicContent.drawLine((int) (p.center * multConst), 30,
						(int) (p.right * multConst), 0);
				graphicContent.drawString((i++) + ".",
						(int) (p.center * multConst) - 5, 42);
			}
		}

		graphicAxis.drawImage(content, 35, 5, null);

		graphicAxis.setColor(Color.BLACK);
		graphicAxis.drawRect(35, 5, content.getWidth(), content.getHeight());

		for (int ax = 0; ax < content.getWidth(); ax += 50) {
			graphicAxis.drawString(new Integer(ax).toString(), ax + 35,
					axis.getHeight() - 10);
			graphicAxis.drawLine(ax + 35, content.getHeight() + 5, ax + 35,
					content.getHeight() + 15);
		}

		for (int ay = 0; ay < content.getHeight(); ay += 20) {
			graphicAxis.drawString(
					new Integer(new Float((1 - ((float) ay / content
							.getHeight())) * 100).intValue()).toString() + "%",
					1, ay + 15);
			graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
		}
		graphicContent.dispose();
		graphicAxis.dispose();
		return axis;
	}

	public BufferedImage renderVertically(int width, int height) {
		BufferedImage content = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		BufferedImage axis = new BufferedImage(width + 10, height + 40,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D graphicContent = content.createGraphics();
		Graphics2D graphicAxis = axis.createGraphics();

		Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
		graphicAxis.setColor(Color.LIGHT_GRAY);
		graphicAxis.fill(backRect);
		graphicAxis.draw(backRect);
		backRect = new Rectangle(0, 0, width, height);
		graphicContent.setColor(Color.WHITE);
		graphicContent.fill(backRect);
		graphicContent.draw(backRect);

		int x, y, x0, y0;
		x = width;
		y = 0;

		graphicContent.setColor(Color.GREEN);

		for (int i = 0; i < yValues.size(); i++) {
			x0 = x;
			y0 = y;
			y = (int) (((float) i / yValues.size()) * height);
			x = (int) ((yValues.elementAt(i) / this.getMaxValue()) * width);
			graphicContent.drawLine(x0, y0, x, y);
		}

		if (peaks != null) { // uz boli vyhladane aj peaky, renderujeme aj
								// tie
			graphicContent.setColor(Color.RED);
			int i = 0;
			double multConst = (double) height / yValues.size();
			for (Peak p : peaks) {
				graphicContent.drawLine(width, (int) (p.left * multConst),
						width - 30, (int) (p.center * multConst));
				graphicContent.drawLine(width - 30,
						(int) (p.center * multConst), width,
						(int) (p.right * multConst));
				graphicContent.drawString((i++) + ".", width - 38,
						(int) (p.center * multConst) + 5);
			}
		}

		graphicAxis.drawImage(content, 5, 5, null);

		graphicAxis.setColor(Color.BLACK);
		graphicAxis.drawRect(5, 5, content.getWidth(), content.getHeight());

		// for (int ax = 0; ax < content.getWidth(); ax += 50) {
		// graphicAxis.drawString(new Integer(ax).toString() , ax + 35,
		// axis.getHeight()-10);
		// graphicAxis.drawLine(ax+35, content.getHeight()+5 ,ax+35,
		// content.getHeight()+15);
		// }
		//
		// for (int ay = 0; ay < content.getHeight(); ay += 20) {
		// graphicAxis.drawString(
		// new Integer(new
		// Float((1-(float)ay/content.getHeight())*100).intValue()).toString() +
		// "%"
		// , 1 ,ay + 15);
		// graphicAxis.drawLine(25,ay+5,35,ay+5);
		// }
		graphicContent.dispose();
		graphicAxis.dispose();
		return axis;
	}

	public void rankFilter(int size) {
		int halfSize = size / 2;
		// Vector<Float> clone = (Vector<Float>)this.yValues.clone();
		Vector<Float> clone = new Vector<Float>(yValues);

		for (int i = halfSize; i < (yValues.size() - halfSize); i++) {
			float sum = 0;
			for (int ii = i - halfSize; ii < (i + halfSize); ii++) {
				sum += clone.elementAt(ii);
			}
			yValues.setElementAt(sum / size, i);
		}

	}

	public int indexOfLeftPeakRel(int peak, double peakFootConstantRel) {
		int index = peak;
		for (int i = peak; i >= 0; i--) {
			index = i;
			if (yValues.elementAt(index) < (peakFootConstantRel * yValues
					.elementAt(peak))) {
				break;
			}
		}
		return Math.max(0, index);
	}

	public int indexOfRightPeakRel(int peak, double peakFootConstantRel) {
		int index = peak;
		for (int i = peak; i < yValues.size(); i++) {
			index = i;
			if (yValues.elementAt(index) < (peakFootConstantRel * yValues
					.elementAt(peak))) {
				break;
			}
		}
		return Math.min(yValues.size(), index);
	}

	public float averagePeakDiff(Vector<Peak> peaks) { // not used
		float sum = 0;
		for (Peak p : peaks) {
			sum += p.getDiff();
		}
		return sum / peaks.size();
	}

	public float maximumPeakDiff(Vector<Peak> peaks, int from, int to) {
		float max = 0;
		for (int i = from; i <= to; i++) {
			max = Math.max(max, peaks.elementAt(i).getDiff());
		}
		return max;
	}

}
