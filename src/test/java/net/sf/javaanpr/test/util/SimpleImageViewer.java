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

package net.sf.javaanpr.test.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 * A simple utility class to view an image - opens a GUI window (JFrame).
 */
class SimpleImageViewer {

    private final JFrame frame;
    private final BufferedImage img;

    public SimpleImageViewer(BufferedImage img) {
        this.img = img;
        frame = new JFrame("WINDOW");
        frame.setVisible(true);
        start();
        frame.add(new JLabel(new ImageIcon(getImage())));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private Image getImage() {
        return img;
    }

    private void start() {
        while (true) {
            BufferStrategy bs = frame.getBufferStrategy();
            if (bs == null) {
                frame.createBufferStrategy(4);
                return;
            }
            Graphics g = bs.getDrawGraphics();
            g.drawImage(img, 0, 0, 800, 600, null);
            g.dispose();
            bs.show();
        }
    }
}
