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

package net.sf.javaanpr.recognizer;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class RecognizedChar {
    private final List<RecognizedPattern> patterns;
    private boolean isSorted;

    public RecognizedChar() {
        this.patterns = new ArrayList<RecognizedPattern>();
        this.isSorted = false;
    }

    public void addPattern(RecognizedPattern pattern) {
        this.patterns.add(pattern);
    }

    public boolean isSorted() {
        return this.isSorted;
    }

    public void sort(boolean shouldSortDescending) {
        if (this.isSorted) {
            return;
        }
        this.isSorted = true;
        Collections.sort(this.patterns, new PatternComparator(shouldSortDescending));
    }

    /**
     * @return null if not sorted
     */
    public List<RecognizedPattern> getPatterns() {
        if (this.isSorted) {
            return this.patterns;
        }
        return null;
    }

    public RecognizedPattern getPattern(int i) {
        if (this.isSorted) {
            return this.patterns.get(i);
        }
        return null;
    }

    public BufferedImage render() {
        int width = 500;
        int height = 200;
        BufferedImage histogram = new BufferedImage(width + 20, height + 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphic = histogram.createGraphics();
        graphic.setColor(Color.LIGHT_GRAY);
        Rectangle backRect = new Rectangle(0, 0, width + 20, height + 20);
        graphic.fill(backRect);
        graphic.draw(backRect);
        graphic.setColor(Color.BLACK);

        int colWidth = width / this.patterns.size();
        int left, top;
        for (int ay = 0; ay <= 100; ay += 10) {
            int y = 15 + (int) (((100 - ay) / 100.0f) * (height - 20));
            graphic.drawString(Integer.toString(ay), 3, y + 11);
            graphic.drawLine(25, y + 5, 35, y + 5);
        }
        graphic.drawLine(35, 19, 35, height);
        graphic.setColor(Color.BLUE);
        for (int i = 0; i < this.patterns.size(); i++) {
            left = (i * colWidth) + 42;
            top = height - (int) (this.patterns.get(i).getCost() * (height - 20));
            graphic.drawRect(left, top, colWidth - 2, height - top);
            graphic.drawString(this.patterns.get(i).getChar() + " ", left + 2, top - 8);
        }
        return histogram;
    }
}
