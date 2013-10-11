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
//import java.awt.image.ConvolveOp;
//import java.awt.image.Kernel;
import java.io.IOException;
import java.util.Vector;

//import org.omg.CORBA.TIMEOUT;
import javaanpr.intelligence.Intelligence;
import javaanpr.recognizer.CharacterRecognizer;

public class Char extends Photo {

	public boolean normalized = false;
	public PositionInPlate positionInPlate = null;

	// private PixelMap pixelMap;
	private PixelMap.Piece bestPiece = null;

	public int fullWidth, fullHeight, pieceWidth, pieceHeight;

	public float statisticAverageBrightness;
	public float statisticMinimumBrightness;
	public float statisticMaximumBrightness;
	public float statisticContrast;
	public float statisticAverageHue;
	public float statisticAverageSaturation;

	public BufferedImage thresholdedImage;

	public Char() {
		image = null;
		init();
	}

	public Char(BufferedImage bi, BufferedImage thresholdedImage,
			PositionInPlate positionInPlate) {
		super(bi);
		this.thresholdedImage = thresholdedImage;
		this.positionInPlate = positionInPlate;
		init();
	}

	public Char(BufferedImage bi) {
		this(bi, bi, null);
		init();
	}

	// nacita znak zo suboru a hned vykona aj thresholding
	// prahovanie(thresholding) sa vacsinou u znakov nerobi, pretoze znaky sa
	// vysekavaju
	// zo znacky, ktora uz je sama o sebe prahovana, ale nacitavanie zo suboru
	// tomuto
	// principu nezodpoveda, cize spravime prahovanie zvlast :
	public Char(String filepath) throws IOException {
		super(filepath);
		// this.thresholdedImage = this.image; povodny kod, zakomentovany dna
		// 23.12.2006 2:33 AM

		// nasledovne 4 riadky pridane 23.12.2006 2:33 AM
		BufferedImage origin = Photo.duplicateBufferedImage(image);
		adaptiveThresholding(); // s ucinnostou nad this.image
		thresholdedImage = image;
		image = origin;

		init();
	}

	@Override
	public Char clone() {
		return new Char(duplicateBufferedImage(image),
				duplicateBufferedImage(thresholdedImage), positionInPlate);
	}

	private void init() {
		fullWidth = super.getWidth();
		fullHeight = super.getHeight();
	}

	public void normalize() {

		if (normalized) {
			return;
		}

		BufferedImage colorImage = duplicateBufferedImage(getBi());
		image = thresholdedImage;

		/*
		 * NEBUDEME POUZIVAT // tu treba osetrit pripady, ked je prvy alebo
		 * posledny riadok cely cierny (zmenime na biely) boolean flag = false;
		 * for (int x=0; x<this.getWidth(); x++) if (this.getBrightness(x,0) >
		 * 0.5f) flag = true; if (flag == false) for (int x=0;
		 * x<this.getWidth(); x++) this.setBrightness(x,0,1.0f);
		 */
		PixelMap pixelMap = getPixelMap();

		bestPiece = pixelMap.getBestPiece();

		colorImage = getBestPieceInFullColor(colorImage, bestPiece);

		// vypocet statistik
		computeStatisticBrightness(colorImage);
		computeStatisticContrast(colorImage);
		computeStatisticHue(colorImage);
		computeStatisticSaturation(colorImage);

		image = bestPiece.render();

		if (image == null) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}

		pieceWidth = super.getWidth();
		pieceHeight = super.getHeight();

		normalizeResizeOnly();
		normalized = true;
	}

	private BufferedImage getBestPieceInFullColor(BufferedImage bi,
			PixelMap.Piece piece) {
		if ((piece.width <= 0) || (piece.height <= 0)) {
			return bi;
		}
		return bi.getSubimage(piece.mostLeftPoint, piece.mostTopPoint,
				piece.width, piece.height);
	}

	private void normalizeResizeOnly() { // vracia ten isty Char, nie novy

		int x = Intelligence.configurator
				.getIntProperty("char_normalizeddimensions_x");
		int y = Intelligence.configurator
				.getIntProperty("char_normalizeddimensions_y");
		if ((x == 0) || (y == 0)) {
			return;// nebude resize
			// this.linearResize(x,y);
		}

		if (Intelligence.configurator.getIntProperty("char_resizeMethod") == 0) {
			linearResize(x, y); // radsej weighted average
		} else {
			averageResize(x, y);
		}

		normalizeBrightness(0.5f);
	}

	// /////////////////////////////////////////////////////
	private void computeStatisticContrast(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Math.abs(statisticAverageBrightness
						- Photo.getBrightness(bi, x, y));
			}
		}
		statisticContrast = sum / (w * h);
	}

	private void computeStatisticBrightness(BufferedImage bi) {
		float sum = 0;
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;

		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float value = Photo.getBrightness(bi, x, y);
				sum += value;
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}
		statisticAverageBrightness = sum / (w * h);
		statisticMinimumBrightness = min;
		statisticMaximumBrightness = max;
	}

	private void computeStatisticHue(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Photo.getHue(bi, x, y);
			}
		}
		statisticAverageHue = sum / (w * h);
	}

	private void computeStatisticSaturation(BufferedImage bi) {
		float sum = 0;
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				sum += Photo.getSaturation(bi, x, y);
			}
		}
		statisticAverageSaturation = sum / (w * h);
	}

	public PixelMap getPixelMap() {
		return new PixelMap(this);
	}

	// //////

	public Vector<Double> extractEdgeFeatures() {
		int w = image.getWidth();
		int h = image.getHeight();
		double featureMatch;

		float[][] array = bufferedImageToArrayWithBounds(image, w, h);
		w += 2; // pridame okraje
		h += 2;

		float[][] features = CharacterRecognizer.features;
		// Vector<Double> output = new Vector<Double>(features.length*4);
		double[] output = new double[features.length * 4];

		for (int f = 0; f < features.length; f++) { // cez vsetky features
			for (int my = 0; my < (h - 1); my++) {
				for (int mx = 0; mx < (w - 1); mx++) { // dlazdice x 0,2,4,..8
														// vcitane
					featureMatch = 0;
					featureMatch += Math.abs(array[mx][my] - features[f][0]);
					featureMatch += Math
							.abs(array[mx + 1][my] - features[f][1]);
					featureMatch += Math
							.abs(array[mx][my + 1] - features[f][2]);
					featureMatch += Math.abs(array[mx + 1][my + 1]
							- features[f][3]);

					int bias = 0;
					if (mx >= (w / 2)) {
						bias += features.length; // ak je v kvadrante napravo ,
					}
					// posunieme bias o jednu
					// triedu
					if (my >= (h / 2)) {
						bias += features.length * 2; // ak je v dolnom
					}
					// kvadrante, posuvame
					// bias o 2 triedy
					output[bias + f] += featureMatch < 0.05 ? 1 : 0;
				} // end my
			} // end mx
		} // end f
		Vector<Double> outputVector = new Vector<Double>();
		for (Double value : output) {
			outputVector.add(value);
		}
		return outputVector;
	}

	public Vector<Double> extractMapFeatures() {
		Vector<Double> vectorInput = new Vector<Double>();
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				vectorInput.add(new Double(this.getBrightness(x, y)));
			}
		}
		return vectorInput;
	}

	public Vector<Double> extractFeatures() {
		int featureExtractionMethod = Intelligence.configurator
				.getIntProperty("char_featuresExtractionMethod");
		if (featureExtractionMethod == 0) {
			return extractMapFeatures();
		} else {
			return extractEdgeFeatures();
		}
	}

}
