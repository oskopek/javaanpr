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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class HoughTransformation {
	public static int RENDER_ALL = 1;
	public static int RENDER_TRANSFORMONLY = 0;
	public static int COLOR_BW = 0;
	public static int COLOR_HUE = 1;

	float[][] bitmap;
	Point maxPoint;
	private int width;
	private int height;

	public float angle = 0;
	public float dx = 0;
	public float dy = 0;

	public HoughTransformation(int width, int height) {
		maxPoint = null;
		bitmap = new float[width][height];
		this.width = width;
		this.height = height;
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				bitmap[x][y] = 0;
			}
		}
	}

	public void addLine(int x, int y, float brightness) {
		// posunieme suradnicovu sustavu do stredu : -1 .. 1, -1 .. 1
		float xf = ((2 * ((float) x)) / width) - 1;
		float yf = ((2 * ((float) y)) / height) - 1;
		// y=ax + b
		// b = y - ax

		for (int a = 0; a < width; a++) {
			// posunieme a do stredu
			float af = ((2 * ((float) a)) / width) - 1;
			// vypocitame b
			float bf = yf - (af * xf);
			// b posumieme do povodneho suradnicoveho systemu
			int b = (int) (((bf + 1) * height) / 2);

			if ((0 < b) && (b < (height - 1))) {
				bitmap[a][b] += brightness;
			}
		}
	}

	/*
	private float getMaxValue() {
		float maxValue = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				maxValue = Math.max(maxValue, bitmap[x][y]);
			}
		}
		return maxValue;
	}
	*/

	private Point computeMaxPoint() {
		float max = 0;
		int maxX = 0, maxY = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float curr = bitmap[x][y];
				if (curr >= max) {
					maxX = x;
					maxY = y;
					max = curr;
				}
			}
		}
		return new Point(maxX, maxY);
	}

	public Point getMaxPoint() {
		if (maxPoint == null) {
			maxPoint = computeMaxPoint();
		}
		return maxPoint;
	}

	private float getAverageValue() {
		float sum = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				sum += bitmap[x][y];
			}
		}
		return sum / (width * height);
	}

	public BufferedImage render(int renderType, int colorType) {

		float average = getAverageValue();
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = output.createGraphics();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int value = (int) ((255 * bitmap[x][y]) / average / 3);
				// int value = (int)Math.log(this.bitmap[x][y]*1000);
				value = Math.max(0, Math.min(value, 255));
				if (colorType == HoughTransformation.COLOR_BW) {
					output.setRGB(x, y, new Color(value, value, value).getRGB());
				} else {
					output.setRGB(x, y, Color.HSBtoRGB(
							0.67f - ((((float) value / 255) * 2) / 3), 1.0f,
							1.0f));
				}
			}
		}
		maxPoint = computeMaxPoint();
		g.setColor(Color.ORANGE);

		float a = ((2 * ((float) maxPoint.x)) / width) - 1;
		float b = ((2 * ((float) maxPoint.y)) / height) - 1;
		// int b = this.maxPoint.y;
		float x0f = -1;
		float y0f = (a * x0f) + b;
		float x1f = 1;
		float y1f = (a * x1f) + b;

		int y0 = (int) (((y0f + 1) * height) / 2);
		int y1 = (int) (((y1f + 1) * height) / 2);

		int dx = width;
		int dy = y1 - y0;
		this.dx = dx;
		this.dy = dy;
		angle = (float) ((180 * Math.atan(this.dy / this.dx)) / Math.PI);

		if (renderType == HoughTransformation.RENDER_ALL) {
			g.drawOval(maxPoint.x - 5, maxPoint.y - 5, 10, 10);
			g.drawLine(0, (height / 2) - (dy / 2) - 1, width,
					((height / 2) + (dy / 2)) - 1);
			g.drawLine(0, ((height / 2) - (dy / 2)) + 0, width, (height / 2)
					+ (dy / 2) + 0);
			g.drawLine(0, ((height / 2) - (dy / 2)) + 1, width, (height / 2)
					+ (dy / 2) + 1);
		}

		return output;
	}
}
