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

package net.sf.javaanpr.tools;

import net.sf.javaanpr.imageanalysis.HoughTransformation;
import net.sf.javaanpr.imageanalysis.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class HoughTool {

    private HoughTool() {
        // intentionally empty
    }

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        FileInputStream fis = new FileInputStream(file);
        Photo p = new Photo(fis);
        HoughTransformation hough = p.getHoughTransformation();
        Photo transformed =
                new Photo(hough.render(HoughTransformation.RENDER_TRANSFORMONLY, HoughTransformation.COLOR_HUE));
        transformed.saveImage(args[1]);
        p.close();
        transformed.close();
    }
}
