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
     * Tests {@link ReportGenerator#insertImage(BufferedImage, String, int, int)}
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
        final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
            final InputStream inStream = null;
            final File io = new File("out.txt");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
        final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
        final InputStream inStream = new FileInputStream("src/test/resources/snapshots/test_001.jpg");
        final File io = new File("src/test/resources/out.txt");
        reportGenerator.saveStreamToFile(inStream, io);
        StringBuilder sb = new StringBuilder();
        sb = testUtility.readFile("src/test/resources/out.txt");
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
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/");
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
     * Tests {@link ReportGenerator#saveImage(BufferedImage, String)} with valid input.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testSaveImage_InvalidInput() throws IllegalArgumentException, IOException {
        try {
            final ReportGenerator reportGenerator = new ReportGenerator("src/test/resources/"); //$NON-NLS-1$
            final CarSnapshot carSnapshot = new CarSnapshot("snapshots/test_001.jpg"); //$NON-NLS-1$
            final BufferedImage image = carSnapshot.renderGraph();
            reportGenerator.saveImage(image, "txt"); //$NON-NLS-1$
        } catch (final Exception e) {
            assertEquals("Unsupported file format", e.getMessage()); //$NON-NLS-1$
        }
    }
}