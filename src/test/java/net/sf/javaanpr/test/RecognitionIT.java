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

package net.sf.javaanpr.test;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class RecognitionIT {

    private static final int currentlyCorrectSnapshots = 53;
    private static final Logger logger = LoggerFactory.getLogger(RecognitionIT.class);
    @Rule
    public ErrorCollector recognitionErrors = new ErrorCollector();

    //    TODO 3 Fix for some strange encodings of jpeg images - they don't always load correctly
    //    See: http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file
    //    B/W images load without a problem: for now - using snapshots/test_041.jpg
    @Test
    public void intelligenceSingleTest() throws IOException, ParserConfigurationException, SAXException {
        final String image = "snapshots/test_041.jpg";
        CarSnapshot carSnap = new CarSnapshot(image);
        assertNotNull("carSnap is null", carSnap);
        assertNotNull("carSnap.image is null", carSnap.getImage());
        Intelligence intel = new Intelligence();
        assertNotNull(intel);
        String spz = intel.recognize(carSnap);
        assertNotNull("The licence plate is null - are you sure the image has the correct color space?", spz);
        assertEquals("LM025BD", spz);
        carSnap.close();
    }

    /**
     * Goes through all the test images and checks if they are correctly recognized.
     * <p>
     * This is only an information test right now, doesn't fail.
     *
     * @throws Exception an Exception
     */
    @Test
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
        assertNotNull(snapshots);
        assertTrue(snapshots.length > 0);

        Intelligence intel = new Intelligence();
        assertNotNull(intel);

        int correctCount = 0;
        int counter = 0;
        boolean correct;
        for (File snap : snapshots) {
            correct = false;
            CarSnapshot carSnap = new CarSnapshot(new FileInputStream(snap));
            assertNotNull("carSnap is null", carSnap);
            assertNotNull("carSnap.image is null", carSnap.getImage());

            String snapName = snap.getName();
            String plateCorrect = properties.getProperty(snapName);
            assertNotNull(plateCorrect);

            String numberPlate = intel.recognize(carSnap, false);

            // TODO enable these checks once the test passes
            // Are you sure the image has the correct color space?
            // recognitionErrors.checkThat("The licence plate is null", numberPlate, is(notNullValue()));
            // recognitionErrors.checkThat("The file \"" + snapName + "\" was incorrectly recognized.", numberPlate,
            // is(plateCorrect));

            if (numberPlate != null && numberPlate.equals(plateCorrect)) {
                correctCount++;
                correct = true;
            }
            carSnap.close();
            counter++;
            logger.debug("Finished recognizing {} ({} of {})\t{}", snapName, counter, snapshots.length,
                    correct ? "correct" : "incorrect");
        }
        logger.info("Correct images: {}, total images: {}, accuracy: {}%", correctCount, snapshots.length,
                (float) correctCount / (float) snapshots.length * 100f);
        assertEquals(currentlyCorrectSnapshots, correctCount);
    }
}
