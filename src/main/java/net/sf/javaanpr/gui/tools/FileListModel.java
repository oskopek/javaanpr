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

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.Vector;

public class FileListModel implements ListModel<Object> {
    public class FileListModelEntry {
        public String fileName;
        public String fullPath;
        public String recognizedPlate;

        public FileListModelEntry(String fileName, String fullPath) {
            this.fileName = fileName;
            this.fullPath = fullPath;
            this.recognizedPlate = "?";
        }

        @Override
        public String toString() {
            return this.fileName;
        }
    }

    public Vector<FileListModelEntry> fileList;

    /**
     * Creates a new instance of FileListModel.
     */
    public FileListModel() {
        this.fileList = new Vector<FileListModelEntry>();
    }

    public void addFileListModelEntry(String fileName, String fullPath) {
        this.fileList.add(new FileListModelEntry(fileName, fullPath));
    }

    @Override
    public int getSize() {
        return this.fileList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return this.fileList.elementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

}
