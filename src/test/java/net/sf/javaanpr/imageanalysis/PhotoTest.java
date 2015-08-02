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

import net.sf.javaanpr.configurator.Configurator;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class PhotoTest {

    @Test
    public void cloneTest() throws Exception {
        InputStream fstream = Configurator.getConfigurator().getResourceAsStream("snapshots/test_041.jpg");
        assertNotNull(fstream);
        Photo photo = new Photo(fstream);
        fstream.close();

        assertNotNull(photo);
        assertNotNull(photo.getImage());

        Photo clone = photo.clone();
        assertEquals(photo, clone);
        assertEquals(photo.hashCode(), clone.hashCode());
        clone.close();
        photo.close();
    }
}
