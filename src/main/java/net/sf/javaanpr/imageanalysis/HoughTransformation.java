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

public class HoughTransformation {

    public static final int RENDER_ALL = 1;
    public static final int RENDER_TRANSFORMONLY = 0;
    public static final int COLOR_BW = 0;
    public static final int COLOR_HUE = 1;
    private float angle = 0;
    private float dx = 0;
    private float dy = 0;
    private final float[][] bitmap;
    private Point maxPoint;
    private final int width;
    private final int height;

    public HoughTransformation(int width, int height) {
        maxPoint = null;
        bitmap = new float[width][height];
        this.width = width;
        this.height = height;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap[x][y] = 0;
            }
        }
    }

    public void addLine(int x, int y, float brightness) {
        // move the coordinate system to the center: -1 .. 1, -1 .. 1
        float xf = ((2 * ((float) x)) / width) - 1;
        float yf = ((2 * ((float) y)) / height) - 1;
        for (int a = 0; a < width; a++) {
            // move a to the center
            float af = ((2 * ((float) a)) / width) - 1;
            // compute b
            float bf = yf - (af * xf);
            // move b to the center of the original coordinate system
            int b = (int) (((bf + 1) * height) / 2);
            if ((0 < b) && (b < (height - 1))) {
                bitmap[a][b] += brightness;
            }
        }
    }

    private Point computeMaxPoint() {
        float max = 0;
        int maxX = 0, maxY = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float curr = bitmap[x][y];
                if (curr >= max) {
                    maxX = x;
                    maxY = y;
                    max = curr;
                }
            }
        }
        return new Point(maxX, maxY);
    }

    public Point getMaxPoint() {
        if (maxPoint == null) {
            maxPoint = computeMaxPoint();
        }
        return maxPoint;
    }

    private float getAverageValue() {
        float sum = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += bitmap[x][y];
            }
        }
        return sum / (width * height);
    }

    public BufferedImage render(int renderType, int colorType) {
        float average = getAverageValue();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int value = (int) ((255 * bitmap[x][y]) / average / 3);
                value = Math.max(0, Math.min(value, 255));
                if (colorType == HoughTransformation.COLOR_BW) {
                    output.setRGB(x, y, new Color(value, value, value).getRGB());
                } else {
                    output.setRGB(x, y, Color.HSBtoRGB(0.67f - ((((float) value / 255) * 2) / 3), 1.0f, 1.0f));
                }
            }
        }
        maxPoint = computeMaxPoint();
        g.setColor(Color.ORANGE);
        float a = ((2 * ((float) maxPoint.x)) / width) - 1;
        float b = ((2 * ((float) maxPoint.y)) / height) - 1;
        float x0f = -1;
        float y0f = (a * x0f) + b;
        float x1f = 1;
        float y1f = (a * x1f) + b;
        int y0 = (int) (((y0f + 1) * height) / 2);
        int y1 = (int) (((y1f + 1) * height) / 2);
        int dx = width;
        int dy = y1 - y0;
        this.dx = dx;
        this.dy = dy;
        angle = (float) ((180 * Math.atan(dy / dx)) / Math.PI);
        if (renderType == HoughTransformation.RENDER_ALL) {
            g.drawOval(maxPoint.x - 5, maxPoint.y - 5, 10, 10);
            g.drawLine(0, (height / 2) - (dy / 2) - 1, width, ((height / 2) + (dy / 2)) - 1);
            g.drawLine(0, ((height / 2) - (dy / 2)) + 0, width, (height / 2) + (dy / 2) + 0);
            g.drawLine(0, ((height / 2) - (dy / 2)) + 1, width, (height / 2) + (dy / 2) + 1);
        }
        return output;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getAngle() {
        return angle;
    }
}
