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

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import net.sf.javaanpr.intelligence.Intelligence;

public class CarSnapshot extends Photo {
	private static int distributor_margins = Intelligence.configurator
			.getIntProperty("carsnapshot_distributormargins");
	// private static int carsnapshot_projectionresize_x =
	// Main.configurator.getIntProperty("carsnapshot_projectionresize_x");
	// private static int carsnapshot_projectionresize_y =
	// Main.configurator.getIntProperty("carsnapshot_projectionresize_y");
	private static int carsnapshot_graphrankfilter = Intelligence.configurator
			.getIntProperty("carsnapshot_graphrankfilter");

	static private int numberOfCandidates = Intelligence.configurator
			.getIntProperty("intelligence_numberOfBands");
	private CarSnapshotGraph graphHandle = null;

	public static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(
			0, 0, CarSnapshot.distributor_margins,
			CarSnapshot.distributor_margins);

	public CarSnapshot() {
	}

	public CarSnapshot(String filepath) throws IOException {
		super(filepath);
	}

	public CarSnapshot(BufferedImage bi) {
		super(bi);
	}
	
	public CarSnapshot(InputStream is) throws IOException {
	    super(is);
	}

	public BufferedImage renderGraph() {
		computeGraph();
		return graphHandle.renderVertically(100, getHeight());
	}

	private Vector<Graph.Peak> computeGraph() {
		if (graphHandle != null) {
			return graphHandle.peaks; // graf uz bol vypocitany
		}

		BufferedImage imageCopy = duplicateBufferedImage(image);
		verticalEdgeBi(imageCopy);
		Photo.thresholding(imageCopy); // strasne moc zere

		graphHandle = histogram(imageCopy);
		graphHandle.rankFilter(CarSnapshot.carsnapshot_graphrankfilter);
		graphHandle.applyProbabilityDistributor(CarSnapshot.distributor);

		graphHandle.findPeaks(CarSnapshot.numberOfCandidates); // sort by height
		return graphHandle.peaks;
	}

	public Vector<Band> getBands() {
		Vector<Band> out = new Vector<Band>();

		Vector<Graph.Peak> peaks = computeGraph();

		for (int i = 0; i < peaks.size(); i++) {
			// vyseknut z povodneho! obrazka znacky, a ulozit do vektora. POZOR
			// !!!!!! Vysekavame z povodneho, takze
			// na suradnice vypocitane z imageCopy musime uplatnit inverznu
			// transformaciu
			Graph.Peak p = peaks.elementAt(i);
			out.add(new Band(image.getSubimage(0, (p.getLeft()),
					image.getWidth(), (p.getDiff()))));
		}
		return out;

	}

	public void verticalEdgeBi(BufferedImage image) {
		BufferedImage imageCopy = Photo.duplicateBufferedImage(image);

		float data[] = { -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1 };

		new ConvolveOp(new Kernel(3, 4, data), ConvolveOp.EDGE_NO_OP, null)
				.filter(imageCopy, image);
	}

	// public void verticalRankBi(BufferedImage image) {
	// BufferedImage imageCopy = duplicateBi(image);
	//
	// float data[] = new float[9];
	// for (int i=0; i<data.length; i++) data[i] = 1.0f/data.length;
	//
	// new ConvolveOp(new Kernel(1,data.length, data), ConvolveOp.EDGE_NO_OP,
	// null).filter(imageCopy, image);
	// }

	public CarSnapshotGraph histogram(BufferedImage bi) {
		CarSnapshotGraph graph = new CarSnapshotGraph(this);
		for (int y = 0; y < bi.getHeight(); y++) {
			float counter = 0;
			for (int x = 0; x < bi.getWidth(); x++) {
				counter += Photo.getBrightness(bi, x, y);
			}
			graph.addPeak(counter);
		}
		return graph;
	}
}
