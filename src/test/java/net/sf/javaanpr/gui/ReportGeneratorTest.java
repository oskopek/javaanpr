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

package net.sf.javaanpr.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.test.util.TestUtility;

import org.junit.Test;

/**
 * Tests the class {@link ReportGenerator}
 */
public class ReportGeneratorTest {
    TestUtility testUtility = new TestUtility();
    private CarSnapshot carSnapshot;

    /**
     * Tests {@link ReportGenerator#insertImage(BufferedImage, String, int, int)} with valid inputs.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testInsertImage_Valid() throws IllegalArgumentException, IOException {
        final int w = 1;
        final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_001.jpg");
        final BufferedImage image = carSnapshot.renderGraph();
        final String cls = "test";
        final int h = 1;
        final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
        try {
            reportGenerator.insertImage(image, cls, w, h);
        } catch (final Exception e) {
            fail();
        }
    }

    /**
     * Tests {@link ReportGenerator#insertImage(BufferedImage, String, int, int)} throws an error when the input is not
     * valid.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testInsertImage_BadInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final int w = 1;
            final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_00.jpg");
            final BufferedImage image = carSnapshot.renderGraph();
            final String cls = "test";
            final int h = 1;
            reportGenerator.insertImage(image, cls, w, h);
        } catch (final Exception e) {
            assertEquals("input == null!", e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#insertText(String)} with null input.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testInsertText_NullInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            reportGenerator.insertText(null);
        } catch (final Exception e) {
            fail();
        }
    }

    /**
     * Tests {@link ReportGenerator#insertText(String)} with empty string input.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testInsertText_EmptyInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            reportGenerator.insertText("");
        } catch (final Exception e) {
            fail();
        }
    }

    /**
     * Tests {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)} with null input stream.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveStreamToFile_InvalidInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final InputStream inStream = null;
            final File io = new File("target/test-classes/out.txt");
            reportGenerator.saveStreamToFile(null, io);
        } catch (final Exception e) {
            assertEquals(null, e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)} with null output stream input.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveStreamToFile_InvalidOutput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final InputStream inStream = new FileInputStream("src/test/resources/snapshots/test_001.jpg");
            reportGenerator.saveStreamToFile(inStream, null);
        } catch (final Exception e) {
            assertEquals(null, e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)} with valid input and output
     * stream.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveStreamToFile_Valid() throws IllegalArgumentException, IOException {
        final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
        final InputStream inStream = new FileInputStream("src/test/resources/snapshots/test_001.jpg");
        final File io = new File("target/test-classes/out.txt");
        reportGenerator.saveStreamToFile(inStream, io);
        StringBuilder sb = new StringBuilder();
        sb = testUtility.readFile("target/test-classes/out.txt");
        assertEquals(true, sb.toString().contains("Hewlett-Packard"));
    }

    /**
     * Tests {@link ReportGenerator#saveImage(BufferedImage, String)} with valid input.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveImage_Valid() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_001.jpg");
            final BufferedImage image = carSnapshot.renderGraph();
            final String cls = "test";
            final int h = 0;
            final int w = 0;
            reportGenerator.saveImage(image, "png");
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#saveImage(BufferedImage, String)} with invalid string input and a valid image input
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveImage_InvalidInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_001.jpg");
            final BufferedImage image = carSnapshot.renderGraph();
            reportGenerator.saveImage(image, "target/test-classes/txt");
        } catch (final Exception e) {
            assertEquals("Unsupported file format", e.getMessage());
        }
    }
}