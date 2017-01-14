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

public class Statistics {

    private final float maximum;
    private final float minimum;
    private final float average;

    public Statistics(Photo photo) {
        float sum = 0;
        int w = photo.getWidth();
        int h = photo.getHeight();
        float maximum = 0;
        float minimum = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float pixelValue = photo.getBrightness(x, y);
                maximum = Math.max(pixelValue, maximum);
                minimum = Math.min(pixelValue, minimum);
                sum += pixelValue;
            }
        }
        int count = (w * h);
        this.maximum = maximum;
        this.minimum = minimum;
        average = sum / count;
    }

    public float thresholdBrightness(float value, float coef) {
        float out;
        if (value > average) {
            out = coef + (((1 - coef) * (value - average)) / (maximum - average));
        } else {
            out = ((1 - coef) * (value - minimum)) / (average - minimum);
        }
        return out;
    }
}
