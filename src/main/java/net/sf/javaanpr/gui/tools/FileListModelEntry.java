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

public class FileListModelEntry {

    private final String fileName;
    private final String fullPath;
    private String recognizedPlate = "?";

    public FileListModelEntry(String fileName, String fullPath) {
        this.fileName = fileName;
        this.fullPath = fullPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getRecognizedPlate() {
        return recognizedPlate;
    }

    public void setRecognizedPlate(String recognizedPlate) {
        this.recognizedPlate = recognizedPlate;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
