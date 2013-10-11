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

//import javaanpr.configurator.Configurator;

public class Band extends Photo {
	static public Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(
			0, 0, 25, 25);
	static private int numberOfCandidates = Intelligence.configurator
			.getIntProperty("intelligence_numberOfPlates");

	private BandGraph graphHandle = null;

	/** Creates a new instance of Band */
	public Band() {
		image = null;
	}

	public Band(BufferedImage bi) {
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
		BufferedImage imageCopy = Photo.duplicateBufferedImage(image);
		fullEdgeDetector(imageCopy);
		graphHandle = histogram(imageCopy);
		graphHandle.rankFilter(image.getHeight());
		graphHandle.applyProbabilityDistributor(Band.distributor);
		graphHandle.findPeaks(Band.numberOfCandidates);
		return graphHandle.peaks;
	}

	public Vector<Plate> getPlates() {
		Vector<Plate> out = new Vector<Plate>();

		Vector<Graph.Peak> peaks = computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			// vyseknut z povodneho! obrazka znacky, a ulozit do vektora. POZOR
			// !!!!!! Vysekavame z povodneho, takze
			// na suradnice vypocitane z imageCopy musime uplatnit inverznu
			// transformaciu
			Graph.Peak p = peaks.elementAt(i);
			out.add(new Plate(image.getSubimage(p.getLeft(), 0, p.getDiff(),
					image.getHeight())));
		}
		return out;
	}

	// public void horizontalRankBi(BufferedImage image) {
	// BufferedImage imageCopy = duplicateBi(image);
	//
	// float data[] = new float[image.getHeight()];
	// for (int i=0; i<data.length; i++) data[i] = 1.0f/data.length;
	//
	// new ConvolveOp(new Kernel(data.length,1, data), ConvolveOp.EDGE_NO_OP,
	// null).filter(imageCopy, image);
	// }

	public BandGraph histogram(BufferedImage bi) {
		BandGraph graph = new BandGraph(this);
		for (int x = 0; x < bi.getWidth(); x++) {
			float counter = 0;
			for (int y = 0; y < bi.getHeight(); y++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}

	public void fullEdgeDetector(BufferedImage source) {
		float verticalMatrix[] = { -1, 0, 1, -2, 0, 2, -1, 0, 1, };
		float horizontalMatrix[] = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };

		BufferedImage i1 = Photo.createBlankBi(source);
		BufferedImage i2 = Photo.createBlankBi(source);

		new ConvolveOp(new Kernel(3, 3, verticalMatrix), ConvolveOp.EDGE_NO_OP,
				null).filter(source, i1);
		new ConvolveOp(new Kernel(3, 3, horizontalMatrix),
				ConvolveOp.EDGE_NO_OP, null).filter(source, i2);

		int w = source.getWidth();
		int h = source.getHeight();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				float sum = 0.0f;
				sum += Photo.getBrightness(i1, x, y);
				sum += Photo.getBrightness(i2, x, y);
				Photo.setBrightness(source, x, y, Math.min(1.0f, sum));
			}
		}

	}

}
