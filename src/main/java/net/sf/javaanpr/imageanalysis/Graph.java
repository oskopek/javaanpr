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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class Graph {

    public Vector<Peak> peaks = null;
    public Vector<Float> yValues = new Vector<Float>();

    // statistical information
    private boolean actualAverageValue = false; // are values up-to-date?
    private boolean actualMaximumValue = false; // are values up-to-date?
    private boolean actualMinimumValue = false; // are values up-to-date?
    private float averageValue;
    private float maximumValue;
    private float minimumValue;

    public void deActualizeFlags() {
        actualAverageValue = false;
        actualMaximumValue = false;
        actualMinimumValue = false;
    }

    /**
     * Detects if the given {@code xPosition} is in the allowed range (with regards to the other peaks).
     *
     * @param peaks peaks already found
     * @param xPosition the position to check
     * @return true if {@code xPosition} is in an allowed range
     */
    public boolean allowedInterval(Vector<Peak> peaks, int xPosition) {
        for (Peak peak : peaks) {
            if ((peak.getLeft() <= xPosition) && (xPosition <= peak.getRight())) {
                return false;
            }
        }
        return true;
    }

    public void addPeak(float value) {
        yValues.add(value);
        deActualizeFlags();
    }

    public void applyProbabilityDistributor(Graph.ProbabilityDistributor probability) {
        yValues = probability.distribute(yValues);
        deActualizeFlags();
    }

    public void negate() {
        float max = getMaxValue();
        for (int i = 0; i < yValues.size(); i++) {
            yValues.setElementAt(max - yValues.elementAt(i), i);
        }
        deActualizeFlags();
    }

    public float getAverageValue() {
        if (!actualAverageValue) {
            averageValue = getAverageValue(0, yValues.size());
            actualAverageValue = true;
        }
        return averageValue;
    }

    public float getAverageValue(int a, int b) {
        float sum = 0.0f;
        for (int i = a; i < b; i++) {
            sum += yValues.elementAt(i).doubleValue();
        }
        return sum / yValues.size();
    }

    public float getMaxValue() {
        if (!actualMaximumValue) {
            maximumValue = getMaxValue(0, yValues.size());
            actualMaximumValue = true;
        }
        return maximumValue;
    }

    public float getMaxValue(int a, int b) {
        float maxValue = 0.0f;
        for (int i = a; i < b; i++) {
            maxValue = Math.max(maxValue, yValues.elementAt(i));
        }
        return maxValue;
    }

    public float getMaxValue(float a, float b) {
        int ia = (int) (a * yValues.size());
        int ib = (int) (b * yValues.size());
        return getMaxValue(ia, ib);
    }

    public int getMaxValueIndex(int a, int b) {
        float maxValue = 0.0f;
        int maxIndex = a;
        for (int i = a; i < b; i++) {
            if (yValues.elementAt(i) >= maxValue) {
                maxValue = yValues.elementAt(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public float getMinValue() {
        if (!actualMinimumValue) {
            minimumValue = getMinValue(0, yValues.size());
            actualMinimumValue = true;
        }
        return minimumValue;
    }

    public float getMinValue(int a, int b) {
        float minValue = Float.POSITIVE_INFINITY;
        for (int i = a; i < b; i++) {
            minValue = Math.min(minValue, yValues.elementAt(i));
        }
        return minValue;
    }

    public float getMinValue(float a, float b) {
        int ia = (int) (a * yValues.size());
        int ib = (int) (b * yValues.size());
        return getMinValue(ia, ib);
    }

    public int getMinValueIndex(int a, int b) {
        float minValue = Float.POSITIVE_INFINITY;
        int minIndex = b;
        for (int i = a; i < b; i++) {
            if (yValues.elementAt(i) <= minValue) {
                minValue = yValues.elementAt(i);
                minIndex = i;
            }
        }
        return minIndex;
    }

    public BufferedImage renderHorizontally(int width, int height) {
        BufferedImage content = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage axis = new BufferedImage(width + 40, height + 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphicContent = content.createGraphics();
        Graphics2D graphicAxis = axis.createGraphics();
        Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
        graphicAxis.setColor(Color.LIGHT_GRAY);
        graphicAxis.fill(backRect);
        graphicAxis.draw(backRect);
        backRect = new Rectangle(0, 0, width, height);
        graphicContent.setColor(Color.WHITE);
        graphicContent.fill(backRect);
        graphicContent.draw(backRect);
        graphicContent.setColor(Color.GREEN);
        int x = 0;
        int y = 0;
        for (int i = 0; i < yValues.size(); i++) {
            int x0 = x;
            int y0 = y;
            x = (int) (((float) i / yValues.size()) * width);
            y = (int) ((1 - (yValues.elementAt(i) / getMaxValue())) * height);
            graphicContent.drawLine(x0, y0, x, y);
        }
        if (peaks != null) { // peaks were already discovered, render them too
            graphicContent.setColor(Color.RED);
            final double multConst = (double) width / yValues.size();
            int i = 0;
            for (Peak p : peaks) {
                graphicContent.drawLine((int) (p.getLeft() * multConst), 0, (int) (p.getCenter() * multConst), 30);
                graphicContent.drawLine((int) (p.getCenter() * multConst), 30, (int) (p.getRight() * multConst), 0);
                graphicContent.drawString(i + ".", (int) (p.getCenter() * multConst) - 5, 42);
                i++;
            }
        }
        graphicAxis.drawImage(content, 35, 5, null);
        graphicAxis.setColor(Color.BLACK);
        graphicAxis.drawRect(35, 5, content.getWidth(), content.getHeight());
        for (int ax = 0; ax < content.getWidth(); ax += 50) {
            graphicAxis.drawString(Integer.toString(ax), ax + 35, axis.getHeight() - 10);
            graphicAxis.drawLine(ax + 35, content.getHeight() + 5, ax + 35, content.getHeight() + 15);
        }
        for (int ay = 0; ay < content.getHeight(); ay += 20) {
            graphicAxis.drawString(
                    Integer.toString(new Float((1 - ((float) ay / content.getHeight())) * 100).intValue()) + "%",
                    1, ay + 15);
            graphicAxis.drawLine(25, ay + 5, 35, ay + 5);
        }
        graphicContent.dispose();
        graphicAxis.dispose();
        return axis;
    }

    public BufferedImage renderVertically(int width, int height) {
        BufferedImage content = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage axis = new BufferedImage(width + 10, height + 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphicContent = content.createGraphics();
        Graphics2D graphicAxis = axis.createGraphics();
        Rectangle backRect = new Rectangle(0, 0, width + 40, height + 40);
        graphicAxis.setColor(Color.LIGHT_GRAY);
        graphicAxis.fill(backRect);
        graphicAxis.draw(backRect);
        backRect = new Rectangle(0, 0, width, height);
        graphicContent.setColor(Color.WHITE);
        graphicContent.fill(backRect);
        graphicContent.draw(backRect);
        int x = width;
        int y = 0;
        graphicContent.setColor(Color.GREEN);
        for (int i = 0; i < yValues.size(); i++) {
            int x0 = x;
            int y0 = y;
            y = (int) (((float) i / yValues.size()) * height);
            x = (int) ((yValues.elementAt(i) / getMaxValue()) * width);
            graphicContent.drawLine(x0, y0, x, y);
        }
        if (peaks != null) { // peaks were already discovered, render them too
            graphicContent.setColor(Color.RED);
            int i = 0;
            double multConst = (double) height / yValues.size();
            for (Peak p : peaks) {
                graphicContent.drawLine(width,
                        (int) (p.getLeft() * multConst),
                        width - 30, (int) (p.getCenter() * multConst));
                graphicContent.drawLine(width - 30,
                        (int) (p.getCenter() * multConst), width,
                        (int) (p.getRight() * multConst));
                graphicContent.drawString(i + ".", width - 38, (int) (p.getCenter() * multConst) + 5);
                i++;
            }
        }
        graphicAxis.drawImage(content, 5, 5, null);
        graphicAxis.setColor(Color.BLACK);
        graphicAxis.drawRect(5, 5, content.getWidth(), content.getHeight());
        graphicContent.dispose();
        graphicAxis.dispose();
        return axis;
    }

    public void rankFilter(int size) {
        int halfSize = size / 2;
        Vector<Float> clone = new Vector<Float>(yValues);
        for (int i = halfSize; i < (yValues.size() - halfSize); i++) {
            float sum = 0;
            for (int ii = i - halfSize; ii < (i + halfSize); ii++) {
                sum += clone.elementAt(ii);
            }
            yValues.setElementAt(sum / size, i);
        }
    }

    public int indexOfLeftPeakRel(int peak, double peakFootConstantRel) {
        int index = peak;
        while (index >= 0) {
            if (yValues.elementAt(index) < (peakFootConstantRel * yValues.elementAt(peak))) {
                break;
            }
            index--;
        }
        return Math.max(0, index);
    }

    public int indexOfRightPeakRel(int peak, double peakFootConstantRel) {
        int index = peak;
        while (index < yValues.size()) {
            if (yValues.elementAt(index) < (peakFootConstantRel * yValues.elementAt(peak))) {
                break;
            }
            index++;
        }
        return Math.min(yValues.size(), index);
    }

    public float averagePeakDiff(Vector<Peak> peaks) {
        float sum = 0;
        for (Peak p : peaks) {
            sum += p.getDiff();
        }
        return sum / peaks.size();
    }

    public float maximumPeakDiff(Vector<Peak> peaks, int from, int to) {
        float max = 0;
        for (int i = from; i <= to; i++) {
            max = Math.max(max, peaks.elementAt(i).getDiff());
        }
        return max;
    }

    public static class ProbabilityDistributor {
        private final float center;
        private final float power;
        private final int leftMargin;
        private final int rightMargin;

        public ProbabilityDistributor(float center, float power, int leftMargin, int rightMargin) {
            this.center = center;
            this.power = power;
            this.leftMargin = Math.max(1, leftMargin);
            this.rightMargin = Math.max(1, rightMargin);
        }

        private float distributionFunction(float value, float positionPercentage) {
            return value * (1 - (power * Math.abs(positionPercentage - center)));
        }

        public Vector<Float> distribute(Vector<Float> peaks) {
            Vector<Float> distributedPeaks = new Vector<Float>();
            for (int i = 0; i < peaks.size(); i++) {
                if ((i < leftMargin) || (i > (peaks.size() - rightMargin))) {
                    distributedPeaks.add(0f);
                } else {
                    distributedPeaks.add(distributionFunction(peaks.elementAt(i), ((float) i / peaks.size())));
                }
            }
            return distributedPeaks;
        }
    }
}
