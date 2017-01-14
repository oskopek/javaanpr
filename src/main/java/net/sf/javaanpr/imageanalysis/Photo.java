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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class Photo implements AutoCloseable, Cloneable {

    private BufferedImage image;

    public Photo(BufferedImage bi) {
        image = bi;
    }

    public Photo(InputStream is) throws IOException {
        loadImage(is);
    }

    public static void setBrightness(BufferedImage image, int x, int y, float value) {
        image.setRGB(x, y, new Color(value, value, value).getRGB());
    }

    public static float getBrightness(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[2];
    }

    public static float getSaturation(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[1];
    }

    public static float getHue(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[0];
    }

    /**
     * Converts a given Image into a BufferedImage.
     *
     * @param img The Image to be converted
     * @return the converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        // Return the buffered image
        return bimage;
    }

    public static BufferedImage linearResizeBi(BufferedImage origin, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        float xScale = (float) width / origin.getWidth();
        float yScale = (float) height / origin.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
        g.drawRenderedImage(origin, at);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage duplicateBufferedImage(BufferedImage image) {
        BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageCopy.setData(image.getData());
        return imageCopy;
    }

    static void thresholding(BufferedImage bi) { // TODO Optimize
        short[] threshold = new short[256];
        for (short i = 0; i < 36; i++) {
            threshold[i] = 0;
        }
        for (short i = 36; i < 256; i++) {
            threshold[i] = i;
        }
        BufferedImageOp thresholdOp = new LookupOp(new ShortLookupTable(0, threshold), null);
        thresholdOp.filter(bi, bi);
    }

    public static BufferedImage arrayToBufferedImage(float[][] array, int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Photo.setBrightness(bi, x, y, array[x][y]);
            }
        }
        return bi;
    }

    public static BufferedImage createBlankBi(BufferedImage image) {
        return new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Photo clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Super clone not supported.");
        }
        return new Photo(duplicateBufferedImage(image));
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getRGB(int x, int y) {
        return image.getRGB(x, y);
    }

    public BufferedImage getImage() {
        return image;
    }

    /**
     * Package-protected!
     * <p>
     * TODO: Should really be private???
     *
     * @param image the new image
     */
    void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getBiWithAxes() {
        BufferedImage axis =
                new BufferedImage(image.getWidth() + 40, image.getHeight() + 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphicAxis = axis.createGraphics();
        graphicAxis.setColor(Color.LIGHT_GRAY);
        Rectangle backRect = new Rectangle(0, 0, image.getWidth() + 40, image.getHeight() + 40);
        graphicAxis.fill(backRect);
        graphicAxis.draw(backRect);
        graphicAxis.drawImage(image, 35, 5, null);
        graphicAxis.setColor(Color.BLACK);
        graphicAxis.drawRect(35, 5, image.getWidth(), image.getHeight());
        for (int ax = 0; ax < image.getWidth(); ax += 50) {
            graphicAxis.drawString(Integer.toString(ax), ax + 35, axis.getHeight() - 10);
            graphicAxis.drawLine(ax + 35, image.getHeight() + 5, ax + 35, image.getHeight() + 15);
        }
        for (int ay = 0; ay < image.getHeight(); ay += 50) {
            graphicAxis.drawString(Integer.toString(ay), 3, ay + 15);
            graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
        }
        graphicAxis.dispose();
        return axis;
    }

    public void setBrightness(int x, int y, float value) {
        image.setRGB(x, y, new Color(value, value, value).getRGB());
    }

    public float getBrightness(int x, int y) {
        return Photo.getBrightness(image, x, y);
    }

    public float getSaturation(int x, int y) {
        return Photo.getSaturation(image, x, y);
    }

    public float getHue(int x, int y) {
        return Photo.getHue(image, x, y);
    }

    public void loadImage(InputStream is) throws IOException {
        BufferedImage image = ImageIO.read(is);
        BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = outImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        this.image = outImage;
    }

    public void saveImage(String filepath) throws IOException {
        String type = filepath.substring(filepath.lastIndexOf('.') + 1, filepath.length()).toUpperCase();
        if (!type.equals("BMP") && !type.equals("JPG") && !type.equals("JPEG") && !type.equals("PNG")) {
            throw new IOException("Unsupported file format");
        }
        File destination = new File(filepath);
        ImageIO.write(image, type, destination);
    }

    public void normalizeBrightness(float coef) {
        Statistics stats = new Statistics(this);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Photo.setBrightness(image, x, y,
                        stats.thresholdBrightness(Photo.getBrightness(image, x, y), coef));
            }
        }
    }

    // FILTERS
    public void linearResize(int width, int height) {
        image = Photo.linearResizeBi(image, width, height);
    }

    public void averageResize(int width, int height) {
        image = averageResizeBi(image, width, height);
    }

    public BufferedImage averageResizeBi(BufferedImage origin, int width, int height) {
        // TODO Doesn't work well for characters of size similar to the target size
        if ((origin.getWidth() < width) || (origin.getHeight() < height)) {
            // average height doesn't play well with zooming in; if we are zooming in in direction x or y,
            // use linear transformation
            return Photo.linearResizeBi(origin, width, height);
        }
        // Java traditionally make images smaller with the bilinear method (linear mapping), which brings large
        // information loss. Fourier transformation would be ideal, but it is too slow.
        // Therefore We use the method of weighted average.
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        float xScale = (float) origin.getWidth() / width;
        float yScale = (float) origin.getHeight() / height;
        for (int x = 0; x < width; x++) {
            int x0min = Math.round(x * xScale);
            int x0max = Math.round((x + 1) * xScale);
            for (int y = 0; y < height; y++) {
                int y0min = Math.round(y * yScale);
                int y0max = Math.round((y + 1) * yScale);
                // do a neigborhood average and save to resizedImage
                float sum = 0;
                int sumCount = 0;
                for (int x0 = x0min; x0 < x0max; x0++) {
                    for (int y0 = y0min; y0 < y0max; y0++) {
                        sum += Photo.getBrightness(origin, x0, y0);
                        sumCount++;
                    }
                }
                sum /= sumCount;
                Photo.setBrightness(resized, x, y, sum);
            }
        }
        return resized;
    }

    public Photo duplicate() {
        return new Photo(Photo.duplicateBufferedImage(image));
    }

    public void verticalEdgeDetector(BufferedImage source) {
        BufferedImage destination = Photo.duplicateBufferedImage(source);
        float[] data1 = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        new ConvolveOp(new Kernel(3, 3, data1), ConvolveOp.EDGE_NO_OP, null).filter(destination, source);
    }

    public float[][] bufferedImageToArray(BufferedImage image, int w, int h) {
        float[][] array = new float[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                array[x][y] = Photo.getBrightness(image, x, y);
            }
        }
        return array;
    }

    public float[][] bufferedImageToArrayWithBounds(BufferedImage image, int w, int h) {
        float[][] array = new float[w + 2][h + 2];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                array[x + 1][y + 1] = Photo.getBrightness(image, x, y);
            }
        }
        // clear the edges
        for (int x = 0; x < (w + 2); x++) {
            array[x][0] = 1;
            array[x][h + 1] = 1;
        }
        for (int y = 0; y < (h + 2); y++) {
            array[0][y] = 1;
            array[w + 1][y] = 1;
        }
        return array;
    }

    public BufferedImage createBlankBi(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Used by edge detectors.
     *
     * @param image1 first image
     * @param image2 second image
     * @return their "sum"
     */
    public BufferedImage sumBufferedImages(BufferedImage image1, BufferedImage image2) {
        BufferedImage out = new BufferedImage(Math.min(image1.getWidth(), image2.getWidth()),
                Math.min(image1.getHeight(), image2.getHeight()), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < out.getWidth(); x++) {
            for (int y = 0; y < out.getHeight(); y++) {
                Photo.setBrightness(out, x, y,
                        (float) Math.min(1.0, Photo.getBrightness(image1, x, y) + Photo.getBrightness(image2, x, y)));
            }
        }
        return out;
    }

    public void plainThresholding(Statistics stat) {
        int width = getWidth();
        int height = getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                setBrightness(x, y, stat.thresholdBrightness(getBrightness(x, y), 1.0f));
            }
        }
    }

    /**
     * Adaptive thresholding through GetNeighborhood.
     *
     * @deprecated The only use of this function should be in the constructor of {@link Plate}.
     */
    public void adaptiveThresholding() {
        Statistics stat = new Statistics(this);
        int radius = Configurator.getConfigurator().getIntProperty("photo_adaptivethresholdingradius");
        if (radius == 0) {
            plainThresholding(stat);
            return;
        }
        int width = getWidth();
        int height = getHeight();
        float[][] sourceArray = bufferedImageToArray(image, width, height);
        float[][] destinationArray = bufferedImageToArray(image, width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // compute neighborhood
                int count = 0;
                float neighborhood = 0.0f;
                for (int ix = x - radius; ix <= (x + radius); ix++) {
                    for (int iy = y - radius; iy <= (y + radius); iy++) {
                        if ((ix >= 0) && (iy >= 0) && (ix < width) && (iy < height)) {
                            neighborhood += sourceArray[ix][iy];
                            count++;
                        }
                    }
                }
                neighborhood /= count;
                if (destinationArray[x][y] < neighborhood) {
                    destinationArray[x][y] = 0f;
                } else {
                    destinationArray[x][y] = 1f;
                }
            }
        }
        image = Photo.arrayToBufferedImage(destinationArray, width, height);
    }

    public HoughTransformation getHoughTransformation() {
        HoughTransformation hough = new HoughTransformation(getWidth(), getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                hough.addLine(x, y, getBrightness(x, y));
            }
        }
        return hough;
    }

    @Override
    public void close() {
        image.flush();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Photo photo = (Photo) o;
        if (getWidth() != photo.getWidth() || getHeight() != photo.getHeight()) {
            return false;
        }
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                if (getRGB(i, j) != getRGB(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        BigInteger rgbSum = BigInteger.ZERO;
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rgbSum = rgbSum.add(BigInteger.valueOf(getRGB(i, j)));
            }
        }
        return rgbSum.hashCode();
    }
}
