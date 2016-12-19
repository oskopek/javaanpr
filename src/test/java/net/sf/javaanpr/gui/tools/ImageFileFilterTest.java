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

package net.sf.javaanpr.gui.tools;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ImageFileFilterTest {
    @Test
    public void testAcceptsJpgFileName() {
        assertTrue(ImageFileFilter.accept("some name.jpg"));
    }

    @Test
    public void testAcceptsBmpFileName() {
        assertTrue(ImageFileFilter.accept("some name.bmp"));
    }

    @Test
    public void testAcceptsGifFileName() {
        assertTrue(ImageFileFilter.accept("some name.gif"));
    }

    @Test
    public void testAcceptsPngFileName() {
        assertTrue(ImageFileFilter.accept("some name.png"));
    }

    @Test
    public void testDoesNotAcceptFileNameWithoutExtension() {
        assertFalse(ImageFileFilter.accept("some name"));
    }

    @Test
    public void testDoesNotAcceptFileNameWithIncorrectExtension() {
        assertFalse(ImageFileFilter.accept("some name.txt"));
    }

    @Test
    public void testAcceptsDirectory() {
        File mockedFile = mock(File.class);
        when(mockedFile.isDirectory()).thenReturn(true);
        assertTrue(new ImageFileFilter().accept(mockedFile));
    }
}
