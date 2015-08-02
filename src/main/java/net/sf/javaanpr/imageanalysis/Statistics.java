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

import java.awt.image.BufferedImage;

public class Statistics {
    public float maximum;
    public float minimum;
    public float average;
    public float dispersion;

    Statistics(BufferedImage bi) {
        this(new Photo(bi));
    }

    Statistics(Photo photo) {
        float sum = 0;
        float sum2 = 0;
        int w = photo.getWidth();
        int h = photo.getHeight();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float pixelValue = photo.getBrightness(x, y);
                this.maximum = Math.max(pixelValue, this.maximum);
                this.minimum = Math.min(pixelValue, this.minimum);
                sum += pixelValue;
                sum2 += (pixelValue * pixelValue);
            }
        }
        int count = (w * h);
        this.average = sum / count;
        // rozptyl = priemer stvorcov + stvorec priemeru
        this.dispersion = (sum2 / count) - (this.average * this.average);
    }

    public float thresholdBrightness(float value, float coef) {
        float out;
        if (value > this.average) {
            out = coef + (((1 - coef) * (value - this.average)) / (this.maximum - this.average));
        } else {
            out = ((1 - coef) * (value - this.minimum)) / (this.average - this.minimum);
        }
        return out;
    }
}
