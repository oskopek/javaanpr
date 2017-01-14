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

import ch.qos.logback.classic.Level;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;
import net.sf.javaanpr.test.util.TestUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore("Is not supposed to be run with regular integration tests.")
public class RecognitionPerformanceIT {

    private static final Logger logger = LoggerFactory.getLogger(RecognitionPerformanceIT.class);
    private Level originalLogLevel;

    private List<CarSnapshot> carSnapshots;
    private Intelligence intelligence;

    @Before
    public void setUp() throws Exception {
        // silence the logger
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("net.sf.javaanpr");
        originalLogLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.INFO);

        carSnapshots = new ArrayList<>();
        String snapshotDirPath = "src/test/resources/snapshots";

        File snapshotDir = new File(snapshotDirPath);
        File[] snapshots = snapshotDir.listFiles();
        assertNotNull(snapshots);
        assertTrue(snapshots.length > 0);

        intelligence = new Intelligence();
        assertNotNull(intelligence);

        for (File snap : snapshots) {
            CarSnapshot carSnap = new CarSnapshot(new FileInputStream(snap));
            assertNotNull("carSnap is null", carSnap);
            assertNotNull("carSnap.image is null", carSnap.getImage());
            carSnapshots.add(carSnap);
            if (carSnapshots.size() >= 5) {
                break;
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME))
                .setLevel(originalLogLevel);
    }

    /**
     * Goes through all the test images and measures the time it took to recognize them all.
     * <p>
     * This is only an information test right now, doesn't fail.
     *
     * @throws Exception an Exception
     */
    @Test
    public void testAllSnapshots() throws Exception {

        final int measurementCount = 2;
        final int repetitions = 100;
        List<Long> timeList = new ArrayList<>();
        List<String> plateList = new ArrayList<>(repetitions * (measurementCount + 5) * carSnapshots.size());
        for (int i = 0; i < measurementCount + 1; i++) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < repetitions; j++) {
                for (CarSnapshot snap : carSnapshots) {
                    String plateText = intelligence.recognize(snap);
                    plateList.add(plateText);
                }
            }

            long end = System.currentTimeMillis();
            long duration = end - start;
            if (i != 0) { // first is a warmup run
                timeList.add(duration);
            }
        }

        DecimalFormat format = new DecimalFormat("#0.00");

        logger.info("Images:\t{}\tTime spent:\t{}ms", carSnapshots.size(),
                format.format(TestUtility.average(timeList)));
        assertEquals(repetitions * (measurementCount + 1) * carSnapshots.size(), plateList.size());
    }
}
