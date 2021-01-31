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
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RecognitionIT {
    private static final long NUM_SNAPSHOTS = 97;
    private static long NUM_ACTUAL_CORRECT = 0;
    private static long NUM_GOLDEN_CORRECT = 0;
    private static final double GOLDEN_ACC = 54.63;

    private static final transient Logger logger = LoggerFactory.getLogger(RecognitionIT.class);

    private static final String SNAPSHOT_DIR_PATH = "src/test/resources/snapshots";
    // TODO: make `results_actual.properties` match `results.properties`.
    private static final String RESULTS_PATH = "src/test/resources/results.properties";
    private static final String RESULTS_ACTUAL_PATH = "src/test/resources/results_actual.properties";

    @Parameterized.Parameters(name = "{0} -> {2} (golden: {1})")
    public static List<Object[]> licensePlates() throws Exception {
        List<Object[]> plates = new ArrayList<>();
        try (InputStream resultsStream = new FileInputStream(RESULTS_PATH);
             InputStream resultsActualStream = new FileInputStream(RESULTS_ACTUAL_PATH)) {
            Properties properties = new Properties();
            properties.load(resultsStream);
            assertTrue(properties.size() > 0);

            Properties propertiesActual = new Properties();
            propertiesActual.load(resultsActualStream);
            assertTrue(propertiesActual.size() > 0);

            for (Object snapNameObj : properties.keySet()) {
                String snapName = snapNameObj.toString();
                String plateCorrect = properties.getProperty(snapName);
                String plateActual = propertiesActual.getProperty(snapName);
                assertNotNull(plateCorrect, plateActual);
                if (plateActual.equals("null")) {
                    plateActual = null;
                }
                plates.add(new Object[]{snapName, plateCorrect, plateActual});
            }
        }
        plates.sort(Comparator.comparing(o -> String.valueOf(o[0])));
        assertEquals(NUM_SNAPSHOTS, plates.size());
        return plates;
    }

    private Intelligence intel;

    @Parameterized.Parameter(0)
    public String snapName;

    @Parameterized.Parameter(1)
    public String plateCorrect;

    @Parameterized.Parameter(2)
    public String plateActual;

    @Before
    public void setUp() throws Exception {
        intel = new Intelligence();
        assertNotNull(intel);
    }

    @BeforeClass
    public static void setUpClass() {
        NUM_ACTUAL_CORRECT = 0;
        NUM_GOLDEN_CORRECT = 0;
    }

    @AfterClass
    public static void tearDownClass() {
        double actualAccuracy = NUM_ACTUAL_CORRECT / (double) NUM_SNAPSHOTS * 100.;
        double goldenAccuracy = NUM_GOLDEN_CORRECT / (double) NUM_SNAPSHOTS * 100.;
        logger.info("Accuracy:\tactual: {}\tgolden:{}",
                String.format("%.2f", actualAccuracy),
                String.format("%.2f", goldenAccuracy));
        assertTrue(String.format("Actual accuracy: %.2f, expected at least 100.0", actualAccuracy),
                actualAccuracy >= 100.0);
        assertTrue(String.format("Golden accuracy: %.2f, expected at least %.2f", goldenAccuracy, GOLDEN_ACC),
                goldenAccuracy >= GOLDEN_ACC);
    }

    private static CarSnapshot loadSnapshot(String path) throws Exception {
        CarSnapshot carSnap = new CarSnapshot(new File(SNAPSHOT_DIR_PATH, path).getAbsolutePath());
        assertNotNull("carSnap is null", carSnap);
        assertNotNull("carSnap.image is null", carSnap.getImage());
        return carSnap;
    }

    //    TODO 3 Fix for some strange encodings of jpeg images - they don't always load correctly
    //    See: http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file
    //    B/W images load without a problem: for now - using snapshots/test_041.jpg
    @Test
    public void testSnapshotWithReport() throws Exception {
        try (CarSnapshot carSnap = loadSnapshot(snapName)) {
            String numberPlate = intel.recognize(carSnap, true);

            assertEquals(plateActual, numberPlate);
        }
    }

    /**
     * Goes through all the test images and checks if they are correctly recognized.
     *
     * <p>If a recognition is null, check if the image has the correct color space.
     *
     * @throws Exception an exception
     */
    @Test
    public void testSnapshot() throws Exception {
        try (CarSnapshot carSnap = loadSnapshot(snapName)) {
            String numberPlate = intel.recognize(carSnap, false);

            NUM_ACTUAL_CORRECT += Objects.equals(plateActual, numberPlate) ? 1 : 0;
            NUM_GOLDEN_CORRECT += Objects.equals(plateCorrect, numberPlate) ? 1 : 0;
            assertEquals(plateActual, numberPlate);
        }
    }
}
