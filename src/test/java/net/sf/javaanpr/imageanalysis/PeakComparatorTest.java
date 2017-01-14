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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeakComparatorTest {
    PeakComparator peakComparator;
    Peak peak1;
    Peak peak2;

    @Before
    public void setup() {
        List<Float> yValues = Arrays.asList(1.0f, 2.0f);
        peakComparator = new PeakComparator(yValues);
        peak1 = mock(Peak.class);
        peak2 = mock(Peak.class);
    }

    @Test
    public void testCompareEqualPeaksReturnsZero() {
        when(peak1.getCenter()).thenReturn(0);
        when(peak2.getCenter()).thenReturn(0);
        assertEquals(peakComparator.compare(peak1, peak2), 0);
    }

    @Test
    public void testCompareFirstPeakSmallerReturnsResultGreaterThanZero() {
        when(peak1.getCenter()).thenReturn(0);
        when(peak2.getCenter()).thenReturn(1);
        int comparisonResult = peakComparator.compare(peak1, peak2);
        assertTrue("Expecting a result which is greater than 0 when comparing peak with centre " + peak1.getCenter()
                + " to peak with centre " + peak2.getCenter() + ". Got result " + comparisonResult,
                comparisonResult > 0);
    }

    @Test
    public void testCompareFirstPeakLargerReturnsResultLessThanZero() {
        when(peak1.getCenter()).thenReturn(1);
        when(peak2.getCenter()).thenReturn(0);
        int comparisonResult = peakComparator.compare(peak1, peak2);
        assertTrue("Expecting a result which is less than 0 when comparing peak with centre " + peak1.getCenter()
                + " to peak with centre " + peak2.getCenter() + ". Got result " + comparisonResult,
                comparisonResult < 0);
    }
}
