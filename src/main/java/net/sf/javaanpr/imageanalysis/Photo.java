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
    public BufferedImage image;

    public Photo(BufferedImage bi) {
        this.image = bi;
    }

    public Photo(InputStream is) throws IOException {
        this.loadImage(is);
    }

    @Override
    public Photo clone() throws CloneNotSupportedException {
        super.clone();
        return new Photo(duplicateBufferedImage(this.image));
    }

    public int getWidth() {
        return this.image.getWidth();
    }

    public int getHeight() {
        return this.image.getHeight();
    }

    public int getRGB(int x, int y) {
        return this.image.getRGB(x, y);
    }

    public BufferedImage getBi() {
        return this.image;
    }

    public BufferedImage getBiWithAxes() {
        BufferedImage axis = new BufferedImage(this.image.getWidth() + 40, this.image.getHeight() + 40,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphicAxis = axis.createGraphics();

        graphicAxis.setColor(Color.LIGHT_GRAY);
        Rectangle backRect = new Rectangle(0, 0, this.image.getWidth() + 40, this.image.getHeight() + 40);
        graphicAxis.fill(backRect);
        graphicAxis.draw(backRect);

        graphicAxis.drawImage(this.image, 35, 5, null);

        graphicAxis.setColor(Color.BLACK);
        graphicAxis.drawRect(35, 5, this.image.getWidth(), this.image.getHeight());

        for (int ax = 0; ax < this.image.getWidth(); ax += 50) {
            graphicAxis.drawString(Integer.toString(ax), ax + 35, axis.getHeight() - 10);
            graphicAxis.drawLine(ax + 35, this.image.getHeight() + 5, ax + 35, this.image.getHeight() + 15);
        }
        for (int ay = 0; ay < this.image.getHeight(); ay += 50) {
            graphicAxis.drawString(Integer.toString(ay), 3, ay + 15);
            graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
        }
        graphicAxis.dispose();
        return axis;
    }

    public void setBrightness(int x, int y, float value) {
        this.image.setRGB(x, y, new Color(value, value, value).getRGB());
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

    public float getBrightness(int x, int y) {
        return Photo.getBrightness(this.image, x, y);
    }

    public float getSaturation(int x, int y) {
        return Photo.getSaturation(this.image, x, y);
    }

    public float getHue(int x, int y) {
        return Photo.getHue(this.image, x, y);
    }

    /**
     * Converts a given Image into a BufferedImage.
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
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

    public void loadImage(InputStream is) throws IOException {
        BufferedImage image = ImageIO.read(is);

        BufferedImage outimage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = outimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        this.image = outimage;

    }

    public void saveImage(String filepath) throws IOException {
        String type = filepath.substring(filepath.lastIndexOf('.') + 1, filepath.length()).toUpperCase();
        if (!type.equals("BMP") && !type.equals("JPG") && !type.equals("JPEG") && !type.equals("PNG")) {
            throw new IOException("Unsupported file format");
        }
        File destination = new File(filepath);
        ImageIO.write(this.image, type, destination);
    }

    public void normalizeBrightness(float coef) {
        Statistics stats = new Statistics(this);
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                Photo.setBrightness(this.image, x, y,
                        stats.thresholdBrightness(Photo.getBrightness(this.image, x, y), coef));
            }
        }
    }

    // FILTERS
    public void linearResize(int width, int height) {
        this.image = Photo.linearResizeBi(this.image, width, height);
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

    public void averageResize(int width, int height) {
        this.image = this.averageResizeBi(this.image, width, height);
    }

    public BufferedImage averageResizeBi(BufferedImage origin, int width, int height) {
        // TODO 2 Nefunguje dobre pre znaky
        // podobnej velkosti ako cielova velkost
        if ((origin.getWidth() < width) || (origin.getHeight() < height)) {
            return Photo.linearResizeBi(origin, width, height); // average
            // height sa
            // nehodi
            // na zvacsovanie, preto ak zvacsujeme v smere x alebo y, pouzijeme
            // radsej linearnu transformaciu
        }

        /*
         * java api standardne zmensuje obrazky bilinearnou metodou, resp. linear mapping. co so sebou prinasa dost
         * velku stratu
         * informacie. Idealna by bola fourierova transformacia, ale ta neprichadza do uvahy z dovodu velkej cesovej
         * narocnosti
         * preto sa ako optimalna javi metoda WEIGHTED AVERAGE
         */
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        float xScale = (float) origin.getWidth() / width;
        float yScale = (float) origin.getHeight() / height;

        for (int x = 0; x < width; x++) {
            int x0min = Math.round(x * xScale);
            int x0max = Math.round((x + 1) * xScale);

            for (int y = 0; y < height; y++) {
                int y0min = Math.round(y * yScale);
                int y0max = Math.round((y + 1) * yScale);

                // spravit priemer okolia a ulozit do resizedImage;
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
                //
            }
        }
        return resized;
    }

    public Photo duplicate() {
        return new Photo(Photo.duplicateBufferedImage(this.image));
    }

    public static BufferedImage duplicateBufferedImage(BufferedImage image) {
        BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageCopy.setData(image.getData());
        return imageCopy;
    }

    static void thresholding(BufferedImage bi) { // TODO 3 Optimalizovat
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

    public void verticalEdgeDetector(BufferedImage source) {
        BufferedImage destination = Photo.duplicateBufferedImage(source);
        float[] data1 = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        // float[] data2 = {1, 0, -1, 2, 0, -2, 1, 0, -1};
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
        // vynulovat hrany :
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

    public BufferedImage createBlankBi(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage sumBi(BufferedImage bi1, BufferedImage bi2) { // used
        // by
        // edgeDetectors
        BufferedImage out = new BufferedImage(Math.min(bi1.getWidth(), bi2.getWidth()), Math.min(bi1.getHeight(),
                bi2.getHeight()), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < out.getWidth(); x++) {
            for (int y = 0; y < out.getHeight(); y++) {
                Photo.setBrightness(out, x, y,
                        (float) Math.min(1.0, Photo.getBrightness(bi1, x, y) + Photo.getBrightness(bi2, x, y)));
            }
        }
        return out;
    }

    public void plainThresholding(Statistics stat) {
        int w = this.getWidth();
        int h = this.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.setBrightness(x, y, stat.thresholdBrightness(this.getBrightness(x, y), 1.0f));
            }
        }
    }

    /**
     * ADAPTIVE THRESHOLDING CEZ GETNEIGHBORHOOD.
     *
     * @deprecated jedine pouzitie tejto funkcie by malo byt v konstruktore znacky
     */
    public void adaptiveThresholding() {
        Statistics stat = new Statistics(this);
        int radius = Configurator.getConfigurator().getIntProperty("photo_adaptivethresholdingradius");
        if (radius == 0) {
            this.plainThresholding(stat);
            return;
        }

        // /
        int w = this.getWidth();
        int h = this.getHeight();

        float[][] sourceArray = this.bufferedImageToArray(this.image, w, h);
        float[][] destinationArray = this.bufferedImageToArray(this.image, w, h);

        int count;
        float neighborhood;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // compute neighborhood
                count = 0;
                neighborhood = 0;
                for (int ix = x - radius; ix <= (x + radius); ix++) {
                    for (int iy = y - radius; iy <= (y + radius); iy++) {
                        if ((ix >= 0) && (iy >= 0) && (ix < w) && (iy < h)) {
                            neighborhood += sourceArray[ix][iy];
                            count++;
                        }
                        /********/
                        // else {
                        // neighborhood += stat.average;
                        // count++;
                        // }
                        /********/
                    }
                }
                neighborhood /= count;
                //
                if (destinationArray[x][y] < neighborhood) {
                    destinationArray[x][y] = 0f;
                } else {
                    destinationArray[x][y] = 1f;
                }
            }
        }
        this.image = Photo.arrayToBufferedImage(destinationArray, w, h);
    }

    public HoughTransformation getHoughTransformation() {
        HoughTransformation hough = new HoughTransformation(this.getWidth(), this.getHeight());
        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                hough.addLine(x, y, this.getBrightness(x, y));
            }
        }
        return hough;
    }

    @Override
    public void close() {
        this.image.flush();
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
        if (this.getWidth() != photo.getWidth() || this.getHeight() != photo.getHeight()) {
            return false;
        }

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                if (this.getRGB(i, j) != this.getRGB(i, j)) {
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
