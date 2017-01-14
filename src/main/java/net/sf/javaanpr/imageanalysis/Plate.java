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
import java.util.ArrayList;
import java.util.List;

public class Plate extends Photo implements Cloneable {

    private static final Graph.ProbabilityDistributor distributor =
            new Graph.ProbabilityDistributor(0, 0, 0, 0);
    private static final int numberOfCandidates =
            Configurator.getConfigurator().getIntProperty("intelligence_numberOfChars");
    private static final int horizontalDetectionType =
            Configurator.getConfigurator().getIntProperty("platehorizontalgraph_detectionType");
    private final Plate plateCopy; // TODO refactor: remove this variable completely
    private PlateGraph graphHandle;

    public Plate(BufferedImage bi) {
        super(bi);
        plateCopy = new Plate(Photo.duplicateBufferedImage(getImage()), true);
        plateCopy.adaptiveThresholding();
    }

    private Plate(BufferedImage bi, boolean isCopy) { // TODO refactor: remove this, is only a copy constructor
        super(bi);
        plateCopy = null;
    }

    public BufferedImage renderGraph() {
        computeGraph();
        return graphHandle.renderHorizontally(getWidth(), 100);
    }

    private List<Peak> computeGraph() {
        if (graphHandle != null) {
            return graphHandle.peaks;
        }
        graphHandle = histogram(plateCopy.getImage());
        graphHandle.applyProbabilityDistributor(Plate.distributor);
        graphHandle.findPeaks(Plate.numberOfCandidates);
        return graphHandle.peaks;
    }

    public List<Char> getChars() {
        List<Char> out = new ArrayList<>();
        List<Peak> peaks = computeGraph();
        for (Peak p : peaks) {
            // Cut from the original image of the plate and save to a vector.
            // ATTENTION: Cutting from original,
            // we have to apply an inverse transformation to the coordinates calculated from imageCopy
            if (p.getDiff() <= 0) {
                continue;
            }
            out.add(new Char(getImage().getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight()),
                    plateCopy.getImage().getSubimage(p.getLeft(), 0, p.getDiff(), getImage().getHeight()),
                    new PositionInPlate(p.getLeft(), p.getRight())));
        }
        return out;
    }

    @Override
    public Plate clone() {
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
        Plate clone1 = clone();
        clone1.verticalEdgeDetector(clone1.getImage());
        PlateVerticalGraph vertical = clone1.histogramYaxis(clone1.getImage());
        setImage(cutTopBottom(getImage(), vertical));
        plateCopy.setImage(cutTopBottom(plateCopy.getImage(), vertical));
        Plate clone2 = clone();
        if (Plate.horizontalDetectionType == 1) {
            clone2.horizontalEdgeDetector(clone2.getImage());
        }
        PlateHorizontalGraph horizontal = clone1.histogramXaxis(clone2.getImage());
        setImage(cutLeftRight(getImage(), horizontal));
        plateCopy.setImage(cutLeftRight(plateCopy.getImage(), horizontal));
    }

    private BufferedImage cutTopBottom(BufferedImage origin, PlateVerticalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
        Peak p = graph.findPeak(3).get(0);
        return origin.getSubimage(0, p.getLeft(), getImage().getWidth(), p.getDiff());
    }

    private BufferedImage cutLeftRight(BufferedImage origin, PlateHorizontalGraph graph) {
        graph.applyProbabilityDistributor(new Graph.ProbabilityDistributor(0f, 0f, 2, 2));
        List<Peak> peaks = graph.findPeak();
        if (peaks.size() != 0) {
            Peak p = peaks.get(0);
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
        PlateVerticalGraph graph = new PlateVerticalGraph();
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
        PlateHorizontalGraph graph = new PlateHorizontalGraph();
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

    public float getCharsWidthDispersion(List<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = getAverageCharWidth(chars);
        for (Char chr : chars) {
            averageDispersion += (Math.abs(averageWidth - chr.fullWidth));
        }
        averageDispersion /= chars.size();
        return averageDispersion / averageWidth;
    }

    public float getPiecesWidthDispersion(List<Char> chars) {
        float averageDispersion = 0;
        float averageWidth = getAveragePieceWidth(chars);
        for (Char chr : chars) {
            averageDispersion += (Math.abs(averageWidth - chr.pieceWidth));
        }
        averageDispersion /= chars.size();
        return averageDispersion / averageWidth;
    }

    public float getAverageCharWidth(List<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) {
            averageWidth += chr.fullWidth;
        }
        averageWidth /= chars.size();
        return averageWidth;
    }

    public float getAveragePieceWidth(List<Char> chars) {
        float averageWidth = 0;
        for (Char chr : chars) {
            averageWidth += chr.pieceWidth;
        }
        averageWidth /= chars.size();
        return averageWidth;
    }

    public float getAveragePieceHue(List<Char> chars) {
        float averageHue = 0;
        for (Char chr : chars) {
            averageHue += chr.statisticAverageHue;
        }
        averageHue /= chars.size();
        return averageHue;
    }

    public float getAveragePieceContrast(List<Char> chars) {
        float averageContrast = 0;
        for (Char chr : chars) {
            averageContrast += chr.statisticContrast;
        }
        averageContrast /= chars.size();
        return averageContrast;
    }

    public float getAveragePieceBrightness(List<Char> chars) {
        float averageBrightness = 0;
        for (Char chr : chars) {
            averageBrightness += chr.statisticAverageBrightness;
        }
        averageBrightness /= chars.size();
        return averageBrightness;
    }

    public float getAveragePieceMinBrightness(List<Char> chars) {
        float averageMinBrightness = 0;
        for (Char chr : chars) {
            averageMinBrightness += chr.statisticMinimumBrightness;
        }
        averageMinBrightness /= chars.size();
        return averageMinBrightness;
    }

    public float getAveragePieceMaxBrightness(List<Char> chars) {
        float averageMaxBrightness = 0;
        for (Char chr : chars) {
            averageMaxBrightness += chr.statisticMaximumBrightness;
        }
        averageMaxBrightness /= chars.size();
        return averageMaxBrightness;
    }

    public float getAveragePieceSaturation(List<Char> chars) {
        float averageSaturation = 0;
        for (Char chr : chars) {
            averageSaturation += chr.statisticAverageSaturation;
        }
        averageSaturation /= chars.size();
        return averageSaturation;
    }

    public float getAverageCharHeight(List<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars) {
            averageHeight += chr.fullHeight;
        }
        averageHeight /= chars.size();
        return averageHeight;
    }

    public float getAveragePieceHeight(List<Char> chars) {
        float averageHeight = 0;
        for (Char chr : chars) {
            averageHeight += chr.pieceHeight;
        }
        averageHeight /= chars.size();
        return averageHeight;
    }
}
