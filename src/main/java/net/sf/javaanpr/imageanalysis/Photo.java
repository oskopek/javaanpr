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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

import javax.imageio.ImageIO;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.intelligence.Intelligence;

public class Photo {
    public BufferedImage image;

    public Photo() {
        image = null;
    }

    public Photo(BufferedImage bi) {
        image = bi;
    }

    public Photo(String filepath) throws IOException {
        loadImage(filepath);
    }

    public Photo(InputStream is) throws IOException {
        loadImage(is);
    }

    @Override
    public Photo clone() {
        return new Photo(duplicateBufferedImage(image));
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getSquare() {
        return getWidth() * getHeight();
    }

    public BufferedImage getBi() {
        return image;
    }

    public BufferedImage getBiWithAxes() {
        BufferedImage axis = new BufferedImage(image.getWidth() + 40,
                image.getHeight() + 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphicAxis = axis.createGraphics();

        graphicAxis.setColor(Color.LIGHT_GRAY);
        Rectangle backRect = new Rectangle(0, 0, image.getWidth() + 40,
                image.getHeight() + 40);
        graphicAxis.fill(backRect);
        graphicAxis.draw(backRect);

        graphicAxis.drawImage(image, 35, 5, null);

        graphicAxis.setColor(Color.BLACK);
        graphicAxis.drawRect(35, 5, image.getWidth(), image.getHeight());

        for (int ax = 0; ax < image.getWidth(); ax += 50) {
            graphicAxis.drawString(new Integer(ax).toString(), ax + 35,
                    axis.getHeight() - 10);
            graphicAxis.drawLine(ax + 35, image.getHeight() + 5, ax + 35,
                    image.getHeight() + 15);
        }
        for (int ay = 0; ay < image.getHeight(); ay += 50) {
            graphicAxis.drawString(new Integer(ay).toString(), 3, ay + 15);
            graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
        }
        graphicAxis.dispose();
        return axis;
    }

    public void setBrightness(int x, int y, float value) {
        image.setRGB(x, y, new Color(value, value, value).getRGB());
    }

    static public void setBrightness(BufferedImage image, int x, int y,
            float value) {
        image.setRGB(x, y, new Color(value, value, value).getRGB());
    }

    static public float getBrightness(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[2];
    }

    static public float getSaturation(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[1];
    }

    static public float getHue(BufferedImage image, int x, int y) {
        int r = image.getRaster().getSample(x, y, 0);
        int g = image.getRaster().getSample(x, y, 1);
        int b = image.getRaster().getSample(x, y, 2);

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return hsb[0];
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

    public void loadImage(String filename) throws IOException {
        String correctedFilepath = Configurator.getConfigurator()
                .correctFilepath(filename);

        BufferedImage image = null;

        if (correctedFilepath == null) {
            File f = new File(filename);

            image = ImageIO.read(f);
        } else {

            InputStream imageIn = getClass().getResourceAsStream(
                    correctedFilepath);

            if (imageIn == null || correctedFilepath == null) {
                throw new IOException("Failed to load image: "
                        + correctedFilepath);
            }

            image = ImageIO.read(imageIn);

        }
        
        BufferedImage outimage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = outimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        this.image = outimage;
    }

    public void loadImage(InputStream is) throws IOException {
        BufferedImage image = ImageIO.read(is);
        BufferedImage outimage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = outimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        this.image = outimage;
    }

    public void saveImage(String filepath) throws IOException {
        String type = new String(filepath.substring(
                filepath.lastIndexOf('.') + 1, filepath.length()).toUpperCase());
        if (!type.equals("BMP") && !type.equals("JPG") && !type.equals("JPEG")
                && !type.equals("PNG")) {
            throw new IOException("Unsupported file format");
        }
        File destination = new File(filepath);
        ImageIO.write(image, type, destination);
    }

    public void normalizeBrightness(float coef) {
        Statistics stats = new Statistics(this);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Photo.setBrightness(
                        image,
                        x,
                        y,
                        stats.thresholdBrightness(
                                Photo.getBrightness(image, x, y), coef));
            }
        }
    }

    // FILTERS
    public void linearResize(int width, int height) {
        image = Photo.linearResizeBi(image, width, height);
    }

    static public BufferedImage linearResizeBi(BufferedImage origin, int width,
            int height) {
        BufferedImage resizedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        float xScale = (float) width / origin.getWidth();
        float yScale = (float) height / origin.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
        g.drawRenderedImage(origin, at);
        g.dispose();
        return resizedImage;
    }

    public void averageResize(int width, int height) {
        image = averageResizeBi(image, width, height);
    }

    // TODO : nefunguje dobre pre znaky podobnej velkosti ako cielvoa velkost
    public BufferedImage averageResizeBi(BufferedImage origin, int width,
            int height) {

        if ((origin.getWidth() < width) || (origin.getHeight() < height)) {
            return Photo.linearResizeBi(origin, width, height); // average
                                                                // height sa
            // nehodi
            // na zvacsovanie, preto ak zvacsujeme v smere x alebo y, pouzijeme
            // radsej linearnu transformaciu
        }

        /*
         * java api standardne zmensuje obrazky bilinearnou metodou, resp.
         * linear mapping. co so sebou prinasa dost velku stratu informacie.
         * Idealna by bola fourierova transformacia, ale ta neprichadza do uvahy
         * z dovodu velkej cesovej narocnosti preto sa ako optimalna javi metoda
         * WEIGHTED AVERAGE
         */
        BufferedImage resized = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

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
        return new Photo(Photo.duplicateBufferedImage(image));
    }

    static public BufferedImage duplicateBufferedImage(BufferedImage image) {
        BufferedImage imageCopy = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageCopy.setData(image.getData());
        return imageCopy;
    }

    static void thresholding(BufferedImage bi) { // TODO: optimalizovat
        short[] threshold = new short[256];
        for (short i = 0; i < 36; i++) {
            threshold[i] = 0;
        }
        for (short i = 36; i < 256; i++) {
            threshold[i] = i;
        }
        BufferedImageOp thresholdOp = new LookupOp(new ShortLookupTable(0,
                threshold), null);
        thresholdOp.filter(bi, bi);
    }

    public void verticalEdgeDetector(BufferedImage source) {
        BufferedImage destination = Photo.duplicateBufferedImage(source);

        float data1[] = { -1, 0, 1, -2, 0, 2, -1, 0, 1, };

        // float data2[] = { 1, 0, -1, 2, 0, -2, 1, 0, -1, };

        new ConvolveOp(new Kernel(3, 3, data1), ConvolveOp.EDGE_NO_OP, null)
                .filter(destination, source);
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

    public float[][] bufferedImageToArrayWithBounds(BufferedImage image, int w,
            int h) {
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

    static public BufferedImage arrayToBufferedImage(float[][] array, int w,
            int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Photo.setBrightness(bi, x, y, array[x][y]);
            }
        }
        return bi;
    }

    static public BufferedImage createBlankBi(BufferedImage image) {
        BufferedImage imageCopy = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
        return imageCopy;
    }

    public BufferedImage createBlankBi(int width, int height) {
        BufferedImage imageCopy = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        return imageCopy;
    }

    public BufferedImage sumBi(BufferedImage bi1, BufferedImage bi2) { // used
                                                                       // by
                                                                       // edgeDetectors
        BufferedImage out = new BufferedImage(Math.min(bi1.getWidth(),
                bi2.getWidth()), Math.min(bi1.getHeight(), bi2.getHeight()),
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < out.getWidth(); x++) {
            for (int y = 0; y < out.getHeight(); y++) {
                Photo.setBrightness(
                        out,
                        x,
                        y,
                        (float) Math.min(1.0, Photo.getBrightness(bi1, x, y)
                                + Photo.getBrightness(bi2, x, y)));
            }
        }
        return out;
    }

    public void plainThresholding(Statistics stat) {
        int w = getWidth();
        int h = getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.setBrightness(x, y, stat.thresholdBrightness(
                        this.getBrightness(x, y), 1.0f));
            }
        }
    }

    /** ADAPTIVE THRESHOLDING CEZ GETNEIGHBORHOOD - deprecated */
    public void adaptiveThresholding() { // jedine pouzitie tejto funkcie by
                                         // malo byt v konstruktore znacky
        Statistics stat = new Statistics(this);
        int radius = Intelligence.configurator
                .getIntProperty("photo_adaptivethresholdingradius");
        if (radius == 0) {
            plainThresholding(stat);
            return;
        }

        // /
        int w = getWidth();
        int h = getHeight();

        float[][] sourceArray = bufferedImageToArray(image, w, h);
        float[][] destinationArray = bufferedImageToArray(image, w, h);

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
        image = Photo.arrayToBufferedImage(destinationArray, w, h);
    }

    public HoughTransformation getHoughTransformation() {
        HoughTransformation hough = new HoughTransformation(getWidth(),
                getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                hough.addLine(x, y, this.getBrightness(x, y));
            }
        }
        return hough;
    }

}
