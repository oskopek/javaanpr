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

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Vector;

public class Plate extends Photo {

    private static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0, 0, 0);
    private static int numberOfCandidates = Configurator.getConfigurator().getIntProperty("intelligence_numberOfChars");
    private static int horizontalDetectionType =
            Configurator.getConfigurator().getIntProperty("platehorizontalgraph_detectionType");
    private Plate plateCopy; // TODO refactor: remove this variable completely
    private PlateGraph graphHandle = null;

    public Plate(BufferedImage bi) {
        super(bi);
        this.plateCopy = new Plate(Photo.duplicateBufferedImage(getImage()), true);
        this.plateCopy.adaptiveThresholding();
    }

    public Plate(BufferedImage bi, boolean isCopy) {
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
        this.graphHandle = this.histogram(this.plateCopy.getImage());
        this.graphHandle.applyProbabilityDistributor(Plate.distributor);
        this.graphHandle.findPeaks(Plate.numberOfCandidates);
        return this.graphHandle.peaks;
    }

    public Vector<Char> getChars() {
        Vector<Char> out = new Vector<Char>();
        Vector<Graph.Peak> peaks = this.computeGraph();
        for (int i = 0; i < peaks.size(); i++) {
            // Cut from the original image of the plate and save to a vector.
            // ATTENTION: Cutting from original,
            // we have to apply an inverse transformation to the coordinates calculated from imageCopy
            Graph.Peak p = peaks.elementAt(i);
            if (p.getDiff() <= 0) {
                continue;
            }
            out.add(new Char(getImage().getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight()),
                    this.plateCopy.getImage().getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight()),
                    new PositionInPlate(p.getLeft(), p.getRight())));
        }
        return out;
    }

    @Override
    public Plate clone() throws CloneNotSupportedException {
        super.clone();
        return new Plate(duplicateBufferedImage(getImage()));
    }

    public void horizontalEdgeBi(BufferedImage image) {
        BufferedImage imageCopy = Photo.duplicateBufferedImage(image);
        float[] data = {-1, 0, 1};
        new ConvolveOp(new Kernel(1, 3, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
    }

    /**
     * Create a clone, normalize it, threshold it with coefficient 0.999.
     *
     * Function {@link Plate#cutTopBottom(BufferedImage, PlateVerticalGraph)} and
     * {@link Plate#cutLeftRight(BufferedImage, PlateHorizontalGraph)} crop the original image using horizontal and
     * vertical projections of the cloned image (which is thresholded).
     */
    public void normalize() {
        Plate clone1 = null;
        try {
            clone1 = this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        clone1.verticalEdgeDetector(clone1.getImage());
        PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getImage());
        this.setImage(cutTopBottom(getImage(), vertical));
        plateCopy.setImage(cutTopBottom(this.plateCopy.getImage(), vertical));
        Plate clone2 = null;
        try {
            clone2 = this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (Plate.horizontalDetectionType == 1) {
            clone2.horizontalEdgeDetector(clone2.getImage());
        }
        PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getImage());
        this.setImage(cutLeftRight(getImage(), horizontal));
        plateCopy.setImage(cutLeftRight(plateCopy.getImage(), horizontal));
    }

    private BufferedImage cutTopBottom(BufferedImage origin, PlateVerticalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
        Graph.Peak p = graph.findPeak(3).elementAt(0);
        return origin.getSubimage(0, p.getLeft(), getImage().getWidth(), p.getDiff());
    }

    private BufferedImage cutLeftRight(BufferedImage origin, PlateHorizontalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
        Vector<Graph.Peak> peaks = graph.findPeak(3);
        if (peaks.size() != 0) {
            Graph.Peak p = peaks.elementAt(0);
            return origin.getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight());
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
        float[] matrix = {-1, 0, 1};
        BufferedImage destination = Photo.duplicateBufferedImage(source);
        new ConvolveOp(new Kernel(3, 1, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
    }

    public void horizontalEdgeDetector(BufferedImage source) {
        BufferedImage destination = Photo.duplicateBufferedImage(source);
        float[] matrix = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
        new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
    }

    public float getCharsWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAverageCharWidth(chars);
        for (Char chr : chars) {
            averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
        }
        averageDispersion /= chars.size();
        return averageDispersion / averageWidth;
    }

    public float getPiecesWidthDispersion(Vector<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = this.getAveragePieceWidth(chars);
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

    public float getAveragePieceHue(Vector<Char> chars) {
        float averageHue = 0;
        for (Char chr : chars) {
            averageHue += chr.statisticAverageHue;
        }
        averageHue /= chars.size();
        return averageHue;
    }

    public float getAveragePieceContrast(Vector<Char> chars) {
        float averageContrast = 0;
        for (Char chr : chars) {
            averageContrast += chr.statisticContrast;
        }
        averageContrast /= chars.size();
        return averageContrast;
    }

    public float getAveragePieceBrightness(Vector<Char> chars) {
        float averageBrightness = 0;
        for (Char chr : chars) {
            averageBrightness += chr.statisticAverageBrightness;
        }
        averageBrightness /= chars.size();
        return averageBrightness;
    }

    public float getAveragePieceMinBrightness(Vector<Char> chars) {
        float averageMinBrightness = 0;
        for (Char chr : chars) {
            averageMinBrightness += chr.statisticMinimumBrightness;
        }
        averageMinBrightness /= chars.size();
        return averageMinBrightness;
    }

    public float getAveragePieceMaxBrightness(Vector<Char> chars) {
        float averageMaxBrightness = 0;
        for (Char chr : chars) {
            averageMaxBrightness += chr.statisticMaximumBrightness;
        }
        averageMaxBrightness /= chars.size();
        return averageMaxBrightness;
    }

    public float getAveragePieceSaturation(Vector<Char> chars) {
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
}
