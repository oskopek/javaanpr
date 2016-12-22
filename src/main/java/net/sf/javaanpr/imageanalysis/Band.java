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
import net.sf.javaanpr.configurator.GlobalState;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Vector;

public class Band extends Photo {

    private static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0, 25, 25);
    private static int numberOfCandidates =
            GlobalState.getInstance().getConfigurator().getIntProperty("intelligence_numberOfPlates");
    private BandGraph graphHandle = null;

    public Band(BufferedImage bi) {
        super(bi);
    }

    public BufferedImage renderGraph() {
        this.computeGraph();
        return this.graphHandle.renderHorizontally(this.getWidth(), 100);
    }

    private Vector<Graph.Peak> computeGraph() {
        if (this.graphHandle != null) {
            return this.graphHandle.peaks;
        }
        BufferedImage imageCopy = Photo.duplicateBufferedImage(getImage());
        this.fullEdgeDetector(imageCopy);
        this.graphHandle = this.histogram(imageCopy);
        this.graphHandle.rankFilter(getImage().getHeight());
        this.graphHandle.applyProbabilityDistributor(Band.distributor);
        this.graphHandle.findPeaks(Band.numberOfCandidates);
        return this.graphHandle.peaks;
    }

    /**
     * Recommended: 3 plates.
     *
     * @return plates
     */
    public Vector<Plate> getPlates() {
        Vector<Plate> out = new Vector<Plate>();
        Vector<Graph.Peak> peaks = this.computeGraph();
        for (int i = 0; i < peaks.size(); i++) {
            // Cut from the original image of the plate and save to a vector.
            // ATTENTION: Cutting from original,
            // we have to apply an inverse transformation to the coordinates calculated from imageCopy
            Graph.Peak p = peaks.elementAt(i);
            out.add(new Plate(getImage().getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight())));
        }
        return out;
    }

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
        float[] verticalMatrix = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        float[] horizontalMatrix = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
        BufferedImage i1 = Photo.createBlankBi(source);
        BufferedImage i2 = Photo.createBlankBi(source);
        new ConvolveOp(new Kernel(3, 3, verticalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i1);
        new ConvolveOp(new Kernel(3, 3, horizontalMatrix), ConvolveOp.EDGE_NO_OP, null).filter(source, i2);
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
