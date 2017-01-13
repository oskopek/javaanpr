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

package net.sf.javaanpr.gui;

import net.sf.javaanpr.configurator.Configurator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ReportGenerator {

    private final String directory;
    private final StringBuilder output; // TODO refactor into a form

    public ReportGenerator(String directory) throws IOException {
        this.directory = directory;
        File f = new File(directory);
        if (!f.exists() && !f.mkdirs()) {
            throw new IOException("Report directory '" + directory + "' doesn't exist and couldn't be created");
        }
        output = new StringBuilder();
        output.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" + "<html>"
                + "<head><title>ANPR report</title>" + "</head>" + "<style type=\"text/css\">"
                + "@import \"style.css\";" + "</style>");
    }

    public void insertText(String text) {
        output.append(text).append("\n");
    }

    public void insertImage(BufferedImage image, String cls, int w, int h)
            throws IllegalArgumentException, IOException {
        String imageName = String.valueOf(image.hashCode()) + ".jpg";
        saveImage(image, imageName);
        if ((w != 0) && (h != 0)) {
            output.append("<img src='").append(imageName).append("' alt='' width='").append(w).append("' height='")
                    .append(h).append("' class='").append(cls).append("'>\n");
        } else {
            output.append("<img src='").append(imageName).append("' alt='' class='").append(cls).append("'>\n");
        }
    }

    public void finish() throws IOException {
        output.append("</html>");
        FileOutputStream os = new FileOutputStream(directory + File.separator + "index.html");
        Writer writer = new OutputStreamWriter(os);
        writer.write(output.toString());
        writer.flush();
        writer.close();
        String cssPath = Configurator.getConfigurator().getPathProperty("reportgeneratorcss");
        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(cssPath);
        saveStreamToFile(inStream, new File(directory + File.separator + "style.css"));
    }

    public void saveStreamToFile(InputStream inStream, File out) throws IOException {
        FileOutputStream outStream = new FileOutputStream(out);
        int read;
        byte[] bytes = new byte[1024];
        while ((read = inStream.read(bytes)) != -1) {
            outStream.write(bytes, 0, read);
        }
        outStream.close();
        inStream.close();
    }

    public void saveImage(BufferedImage bi, String filename) throws IOException, IllegalArgumentException {
        String type = filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase();
        if (!type.equals("bmp") && !type.equals("jpg") && !type.equals("jpeg") && !type.equals("png")) {
            throw new IllegalArgumentException("Unsupported file format");
        }
        File destination = new File(directory + File.separator + filename);
        try {
            ImageIO.write(bi, type, destination);
        } catch (IOException e) {
            throw new IOException("Can't open destination report directory", e);
        }
    }
}
