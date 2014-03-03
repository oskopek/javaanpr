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
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net
for more info about JavaANPR.
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package org.lib.test;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

/**
 *
 */
public class LibraryTest {

    /*
     * TODO 3 Fix for some strange encodings of jpeg images - they don't always load correctly See:
     * http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file B/W images load without
     * a problem: for now - using snapshots/test_041.jpg
     */
    @Test
    public void intelligenceSingleTest() throws IOException, ParserConfigurationException, SAXException {
        final String image = "snapshots/test_041.jpg";

        /*
         * Show raw loaded image for 5s InputStream is = Configurator.getConfigurator().getResourceAsStream(image);
         * BufferedImage bi = ImageIO.read(is); TestImageDraw t = new TestImageDraw(bi); Thread.sleep(5000); is =
         * Configurator.getConfigurator().getResourceAsStream(image);
         */

        /*
         * Show Photo loaded image for 5s Photo p = new Photo(is); t = new TestImageDraw(p.image); Thread.sleep(5000);
         * p.close();
         */

        CarSnapshot carSnap = new CarSnapshot(image);
        assertNotNull("carSnap is null", carSnap);
        assertNotNull("carSnap.image is null", carSnap.image);

        /*
         * Show CarSnapshot loaded image for 5s t = new TestImageDraw(carSnap.image); Thread.sleep(5000); t.frame.dispose();
         */

        Intelligence intel = new Intelligence();
        assertNotNull(intel);

        String spz = intel.recognize(carSnap);
        assertNotNull("The licence plate is null - are you sure the image has the correct color space?", spz);

        // System.out.println(spz);

        assertEquals("LM025BD", spz);

        // System.out.println(intel.lastProcessDuration());
        carSnap.close();
    }

    @Test
    @Ignore
    public void testAllSnapshots() throws Exception {
        String snapshotDirPath = "src/test/resources/snapshots";
        String resultsPath = "src/test/resources/results.properties";
        InputStream resultsStream = new FileInputStream(new File(resultsPath));

        Properties properties = new Properties();
        properties.load(resultsStream);
        resultsStream.close();
        assertTrue(properties.size() > 0);

        File snapshotDir = new File(snapshotDirPath);
        File[] snapshots = snapshotDir.listFiles();
        assertTrue(snapshots.length > 0);

        Intelligence intel = new Intelligence();
        assertNotNull(intel);


        for(File snap : snapshots) {
            CarSnapshot carSnap = new CarSnapshot(new FileInputStream(snap));
            assertNotNull("carSnap is null", carSnap);
            assertNotNull("carSnap.image is null", carSnap.image);

            String snapName = snap.getName();
            String plateCorrect = properties.getProperty(snapName);
            assertNotNull(plateCorrect);

            String numberPlate = intel.recognize(carSnap);
            assertNotNull("The licence plate is null - are you sure the image has the correct color space?", numberPlate);

            assertEquals("The file \"" + snapName + "\" was incorrectly recognized.", plateCorrect, numberPlate);
            carSnap.close();
        }
    }

    public class TestImageDraw {

        public JFrame frame;
        BufferedImage img;
        public int WIDTH = 800;
        public int HEIGHT = 600;

        public TestImageDraw(BufferedImage img) {
            this.img = img;
            this.frame = new JFrame("WINDOW");
            this.frame.setVisible(true);

            this.start();
            this.frame.add(new JLabel(new ImageIcon(this.getImage())));

            this.frame.pack();
            // frame.setSize(WIDTH, HEIGHT);
            // Better to DISPOSE than EXIT
            this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        public Image getImage() {
            return this.img;
        }

        public void start() {

            boolean running = true;
            while (running) {
                BufferStrategy bs = this.frame.getBufferStrategy();
                if (bs == null) {
                    this.frame.createBufferStrategy(4);
                    return;
                }

                Graphics g = bs.getDrawGraphics();
                g.drawImage(this.img, 0, 0, this.WIDTH, this.HEIGHT, null);
                g.dispose();
                bs.show();

            }
        }
    }
}
