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

package net.sf.javaanpr.gui;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.test.util.TestUtility;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Tests the class {@link ReportGenerator}.
 */
public class ReportGeneratorTest {

    /**
     * Tests {@link ReportGenerator#insertImage(BufferedImage, String, int, int)} with valid inputs.
     *
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
     *
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
     *
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
     *
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
     *
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveStreamToFile_InvalidInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final InputStream inStream = null;
            final File io = new File("target/test-classes/out.txt");
            reportGenerator.saveStreamToFile(inStream, io);
        } catch (final Exception e) {
            assertEquals(null, e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#saveStreamToFile(java.io.InputStream, java.io.File)} with null output stream input.
     *
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
     *
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveStreamToFile_Valid() throws IllegalArgumentException, IOException {
        final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
        final InputStream inStream = new FileInputStream("src/test/resources/snapshots/test_001.jpg");
        final File io = new File("target/test-classes/out.txt");
        reportGenerator.saveStreamToFile(inStream, io);
        StringBuilder sb = TestUtility.readFile("target/test-classes/out.txt");
        assertEquals(true, sb.toString().contains("Hewlett-Packard"));
    }

    /**
     * Tests {@link ReportGenerator#saveImage(BufferedImage, String)} with valid input.
     *
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveImage_Valid() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("target/test-classes/");
            final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_001.jpg");
            final BufferedImage image = carSnapshot.renderGraph();
            reportGenerator.saveImage(image, "png");
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests {@link ReportGenerator#saveImage(BufferedImage, String)} with invalid string input and a valid image
     * input.
     *
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
