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

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ImageFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String name = f.getName();
        return accept(name);
    }

    public static boolean accept(String name) {
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex < 0) {
            return false;
        }
        String type = name.substring(lastIndex + 1, name.length()).toLowerCase();
        return type.equals("bmp") || type.equals("jpg") || type.equals("jpeg") || type.equals("png")
                || type.equals("gif");
    }

    @Override
    public String getDescription() {
        return "images (*.jpg, *.bmp, *.gif, *.png)";
    }
}
