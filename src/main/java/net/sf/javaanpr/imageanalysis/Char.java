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
import net.sf.javaanpr.recognizer.CharacterRecognizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Char extends Photo {

    public boolean normalized = false;
    public PositionInPlate positionInPlate = null;

    public int fullWidth, fullHeight, pieceWidth, pieceHeight;

    public float statisticAverageBrightness;
    public float statisticMinimumBrightness;
    public float statisticMaximumBrightness;
    public float statisticContrast;
    public float statisticAverageHue;
    public float statisticAverageSaturation;

    public BufferedImage thresholdedImage;

    public Char(BufferedImage bi, BufferedImage thresholdedImage, PositionInPlate positionInPlate) {
        super(bi);
        this.thresholdedImage = thresholdedImage;
        this.positionInPlate = positionInPlate;
        init();
    }

    public Char(BufferedImage bi) {
        this(bi, bi, null);
        init();
    }

    /**
     * Reads a character from file and executes thresholding.
     * <p>
     * Thresholding isn't usually done on individual characters because they're extracted from the plates
     * which is in itself already thresholded, but loading from a file doesn't follow this principle.
     *
     * @param fileName name of character file
     * @throws IOException if the file couldn't be loaded
     */
    public Char(String fileName) throws IOException {
        super(Configurator.getConfigurator().getResourceAsStream(fileName));
        BufferedImage origin = Photo.duplicateBufferedImage(getImage());
        adaptiveThresholding(); // act on this.image // TODO deprecated
        thresholdedImage = getImage();
        setImage(origin);
        init();
    }

    /**
     * Reads a character from file and executes thresholding.
     * <p>
     * Thresholding isn't usually done on individual characters because they're extracted from the plates
     * which is in itself already thresholded, but loading from a file doesn't follow this principle.
     *
     * @param is loads Char from this InputStream
     * @throws IOException an IOException
     */
    public Char(InputStream is) throws IOException { // TODO javadoc
        super(is);
        BufferedImage origin = Photo.duplicateBufferedImage(getImage());
        adaptiveThresholding(); // act on this.image // TODO deprecated
        thresholdedImage = getImage();
        setImage(origin);
        init();
    }

    private static String getSuffix(String directoryName) {
        if (directoryName.endsWith("/")) {
            directoryName = directoryName.substring(0, directoryName.length() - 1); // cuts last char off
        }
        return directoryName.substring(directoryName.lastIndexOf('_'));
    }

    public static List<String> getAlphabetList(String directory) {
        final String alphaString = "0123456789abcdefghijklmnopqrstuvwxyz";
        final String suffix = getSuffix(directory);
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < alphaString.length(); i++) {
            String s = directory + File.separator + alphaString.charAt(i) + suffix + ".jpg";
            if (Configurator.getConfigurator().getResourceAsStream(s) != null) {
                filenames.add(s);
            }
        }
        return filenames;
    }

    @Override
    public Char clone() {
        super.clone();
        return new Char(duplicateBufferedImage(getImage()), duplicateBufferedImage(thresholdedImage),
                positionInPlate);
    }

    private void init() {
        fullWidth = super.getWidth();
        fullHeight = super.getHeight();
    }

    public void normalize() {
        if (normalized) {
            return;
        }
        BufferedImage colorImage = duplicateBufferedImage(getImage());
        setImage(thresholdedImage);
        PixelMap pixelMap = getPixelMap();
        PixelMap.Piece bestPiece = pixelMap.getBestPiece();
        colorImage = getBestPieceInFullColor(colorImage, bestPiece);

        // Compute statistics
        computeStatisticBrightness(colorImage);
        computeStatisticContrast(colorImage);
        computeStatisticHue(colorImage);
        computeStatisticSaturation(colorImage);

        setImage(bestPiece.render());
        if (getImage() == null) {
            setImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        }
        pieceWidth = super.getWidth();
        pieceHeight = super.getHeight();
        normalizeResizeOnly();
        normalized = true;
    }

    private BufferedImage getBestPieceInFullColor(BufferedImage bi, PixelMap.Piece piece) {
        if ((piece.getWidth() <= 0) || (piece.getHeight() <= 0)) {
            return bi;
        }
        return bi.getSubimage(piece.getMostLeftPoint(), piece.getMostTopPoint(), piece.getWidth(), piece.getHeight());
    }

    private void normalizeResizeOnly() { // returns the same Char object
        int x = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_x");
        int y = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_y");
        if ((x == 0) || (y == 0)) {
            return;
        }
        if (Configurator.getConfigurator().getIntProperty("char_resizeMethod") == 0) {
            linearResize(x, y); // do a weighted average
        } else {
            averageResize(x, y);
        }
        normalizeBrightness(0.5f);
    }

    private void computeStatisticContrast(BufferedImage bi) {
        float sum = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                sum += Math.abs(statisticAverageBrightness - Photo.getBrightness(bi, x, y));
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

    public List<Double> extractEdgeFeatures() {
        int width = getImage().getWidth();
        int height = getImage().getHeight();
        double featureMatch;
        float[][] array = bufferedImageToArrayWithBounds(getImage(), width, height);
        width += 2; // add edges
        height += 2;
        float[][] features = CharacterRecognizer.FEATURES;
        double[] output = new double[features.length * 4];

        for (int f = 0; f < features.length; f++) {
            for (int my = 0; my < (height - 1); my++) {
                for (int mx = 0; mx < (width - 1); mx++) {
                    featureMatch = 0;
                    featureMatch += Math.abs(array[mx][my] - features[f][0]);
                    featureMatch += Math.abs(array[mx + 1][my] - features[f][1]);
                    featureMatch += Math.abs(array[mx][my + 1] - features[f][2]);
                    featureMatch += Math.abs(array[mx + 1][my + 1] - features[f][3]);

                    int bias = 0;
                    if (mx >= (width / 2)) {
                        bias += features.length; // if we are in the right quadrant, move the bias by one class
                    }
                    if (my >= (height / 2)) {
                        bias += features.length * 2; // if we are in the left quadrant, move the bias by two classes
                    }
                    output[bias + f] += featureMatch < 0.05 ? 1 : 0;
                }
            }
        }
        List<Double> outputList = new ArrayList<>();
        for (Double value : output) {
            outputList.add(value);
        }
        return outputList;
    }

    public List<Double> extractMapFeatures() {
        List<Double> vectorInput = new ArrayList<>();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                vectorInput.add((double) getBrightness(x, y));
            }
        }
        return vectorInput;
    }

    public List<Double> extractFeatures() {
        int featureExtractionMethod = Configurator.getConfigurator().getIntProperty("char_featuresExtractionMethod");
        if (featureExtractionMethod == 0) {
            return extractMapFeatures();
        } else {
            return extractEdgeFeatures();
        }
    }


}
