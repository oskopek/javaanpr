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
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
//import java.io.IOException;
import java.util.Vector;

import javaanpr.intelligence.Intelligence;

//import javaanpr.imageanalysis.PositionInPlate;

public class Plate extends Photo {
	static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(
			0, 0, 0, 0);
	static private int numberOfCandidates = Intelligence.configurator
			.getIntProperty("intelligence_numberOfChars");
	private static int horizontalDetectionType = Intelligence.configurator
			.getIntProperty("platehorizontalgraph_detectionType");

	private PlateGraph graphHandle = null;
	public Plate plateCopy;

	/** Creates a new instance of Character */
	public Plate() {
		image = null;
	}

	public Plate(BufferedImage bi) {
		super(bi);
		plateCopy = new Plate(Photo.duplicateBufferedImage(image), true);
		plateCopy.adaptiveThresholding();
	}

	public Plate(BufferedImage bi, boolean isCopy) {
		super(bi);
	}

	public BufferedImage renderGraph() {
		computeGraph();
		return graphHandle.renderHorizontally(getWidth(), 100);
	}

	private Vector<Graph.Peak> computeGraph() {
		if (graphHandle != null) {
			return graphHandle.peaks; // graf uz bol vypocitany
		}

		graphHandle = histogram(plateCopy.getBi()); // PlateGraph graph =
													// histogram(imageCopy);
		graphHandle.applyProbabilityDistributor(Plate.distributor);
		graphHandle.findPeaks(Plate.numberOfCandidates);

		return graphHandle.peaks;
	}

	public Vector<Char> getChars() {
		Vector<Char> out = new Vector<Char>();

		Vector<Graph.Peak> peaks = computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			// vyseknut z povodneho! obrazka znacky, a ulozit do vektora. POZOR
			// !!!!!! Vysekavame z povodneho, takze
			// na suradnice vypocitane z imageCopy musime uplatnit inverznu
			// transformaciu
			Graph.Peak p = peaks.elementAt(i);
			if (p.getDiff() <= 0) {
				continue;
			}
			out.add(new Char(image.getSubimage(p.getLeft(), 0, p.getDiff(),
					image.getHeight()), plateCopy.image.getSubimage(
					p.getLeft(), 0, p.getDiff(), image.getHeight()),
					new PositionInPlate(p.getLeft(), p.getRight())));
		}

		return out;
	}

	@Override
	public Plate clone() {
		return new Plate(duplicateBufferedImage(image));
	}

	public void horizontalEdgeBi(BufferedImage image) {
		BufferedImage imageCopy = Photo.duplicateBufferedImage(image);
		float data[] = { -1, 0, 1 };
		new ConvolveOp(new Kernel(1, 3, data), ConvolveOp.EDGE_NO_OP, null)
				.filter(imageCopy, image);
	}

	public void normalize() {
		// pre ucely orezania obrazka sa vytvori klon ktory sa normalizuje a
		// prahuje s
		// koeficientom 0.999. funkcie cutTopBottom a cutLeftRight orezu
		// originalny
		// obrazok na zaklade horizontalnej a vertikalnej projekcie
		// naklonovaneho
		// obrazka, ktory je prahovany

		Plate clone1 = clone();
		clone1.verticalEdgeDetector(clone1.getBi());
		PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getBi());
		image = cutTopBottom(image, vertical);
		plateCopy.image = cutTopBottom(plateCopy.image, vertical);

		Plate clone2 = clone();
		if (Plate.horizontalDetectionType == 1) {
			clone2.horizontalEdgeDetector(clone2.getBi());
		}
		PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getBi());
		image = cutLeftRight(image, horizontal);
		plateCopy.image = cutLeftRight(plateCopy.image, horizontal);

	}

	private BufferedImage cutTopBottom(BufferedImage origin,
			PlateVerticalGraph graph) {
		graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,
				0f, 2, 2));
		Graph.Peak p = graph.findPeak(3).elementAt(0);
		return origin
				.getSubimage(0, p.getLeft(), image.getWidth(), p.getDiff());
	}

	private BufferedImage cutLeftRight(BufferedImage origin,
			PlateHorizontalGraph graph) {
		graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f,
				0f, 2, 2));
		Vector<Graph.Peak> peaks = graph.findPeak(3);

		if (peaks.size() != 0) {
			Graph.Peak p = peaks.elementAt(0);
			return origin.getSubimage(p.getLeft(), 0, p.getDiff(),
					image.getHeight());
		}
		return origin;
	}

	public PlateGraph histogram(BufferedImage bi) {
		PlateGraph graph = new PlateGraph(this);
		for (int x = 0; x < bi.getWidth(); x++) {
			float counter = 0;
			for (int y = 0; y < bi.getHeight(); y++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	private PlateVerticalGraph histogramYaxis(BufferedImage bi) {
		PlateVerticalGraph graph = new PlateVerticalGraph(this);
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int y = 0; y < h; y++) {
			float counter = 0;
			for (int x = 0; x < w; x++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	private PlateHorizontalGraph histogramXaxis(BufferedImage bi) {
		PlateHorizontalGraph graph = new PlateHorizontalGraph(this);
		int w = bi.getWidth();
		int h = bi.getHeight();
		for (int x = 0; x < w; x++) {
			float counter = 0;
			for (int y = 0; y < h; y++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	@Override
	public void verticalEdgeDetector(BufferedImage source) {

		float matrix[] = { -1, 0, 1 };

		BufferedImage destination = Photo.duplicateBufferedImage(source);

		new ConvolveOp(new Kernel(3, 1, matrix), ConvolveOp.EDGE_NO_OP, null)
				.filter(destination, source);

	}

	public void horizontalEdgeDetector(BufferedImage source) {
		BufferedImage destination = Photo.duplicateBufferedImage(source);

		float matrix[] = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };

		new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null)
				.filter(destination, source);
	}

	public float getCharsWidthDispersion(Vector<Char> chars) {
		float averageDispersion = 0;
		float averageWidth = getAverageCharWidth(chars);

		for (Char chr : chars) {
			averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
		}
		averageDispersion /= chars.size();

		return averageDispersion / averageWidth;
	}

	public float getPiecesWidthDispersion(Vector<Char> chars) {
		float averageDispersion = 0;
		float averageWidth = getAveragePieceWidth(chars);

		for (Char chr : chars) {
			averageDispersion += (Math.abs(averageWidth - chr.pieceWidth));
		}
		averageDispersion /= chars.size();

		return averageDispersion / averageWidth;
	}

	public float getAverageCharWidth(Vector<Char> chars) {
		float averageWidth = 0;
		for (Char chr : chars) {
			averageWidth += chr.fullWidth;
		}
		averageWidth /= chars.size();
		return averageWidth;
	}

	public float getAveragePieceWidth(Vector<Char> chars) {
		float averageWidth = 0;
		for (Char chr : chars) {
			averageWidth += chr.pieceWidth;
		}
		averageWidth /= chars.size();
		return averageWidth;
	}

	public float getAveragePieceHue(Vector<Char> chars) throws Exception {
		float averageHue = 0;
		for (Char chr : chars) {
			averageHue += chr.statisticAverageHue;
		}
		averageHue /= chars.size();
		return averageHue;
	}

	public float getAveragePieceContrast(Vector<Char> chars) throws Exception {
		float averageContrast = 0;
		for (Char chr : chars) {
			averageContrast += chr.statisticContrast;
		}
		averageContrast /= chars.size();
		return averageContrast;
	}

	public float getAveragePieceBrightness(Vector<Char> chars) throws Exception {
		float averageBrightness = 0;
		for (Char chr : chars) {
			averageBrightness += chr.statisticAverageBrightness;
		}
		averageBrightness /= chars.size();
		return averageBrightness;
	}

	public float getAveragePieceMinBrightness(Vector<Char> chars)
			throws Exception {
		float averageMinBrightness = 0;
		for (Char chr : chars) {
			averageMinBrightness += chr.statisticMinimumBrightness;
		}
		averageMinBrightness /= chars.size();
		return averageMinBrightness;
	}

	public float getAveragePieceMaxBrightness(Vector<Char> chars)
			throws Exception {
		float averageMaxBrightness = 0;
		for (Char chr : chars) {
			averageMaxBrightness += chr.statisticMaximumBrightness;
		}
		averageMaxBrightness /= chars.size();
		return averageMaxBrightness;
	}

	public float getAveragePieceSaturation(Vector<Char> chars) throws Exception {
		float averageSaturation = 0;
		for (Char chr : chars) {
			averageSaturation += chr.statisticAverageSaturation;
		}
		averageSaturation /= chars.size();
		return averageSaturation;
	}

	public float getAverageCharHeight(Vector<Char> chars) {
		float averageHeight = 0;
		for (Char chr : chars) {
			averageHeight += chr.fullHeight;
		}
		averageHeight /= chars.size();
		return averageHeight;
	}

	public float getAveragePieceHeight(Vector<Char> chars) {
		float averageHeight = 0;
		for (Char chr : chars) {
			averageHeight += chr.pieceHeight;
		}
		averageHeight /= chars.size();
		return averageHeight;
	}

	// public float getAverageCharSquare(Vector<Char> chars) {
	// float average = 0;
	// for (Char chr : chars)
	// average += chr.getWidth() * chr.getHeight();
	// average /= chars.size();
	// return average;
	// }

}
