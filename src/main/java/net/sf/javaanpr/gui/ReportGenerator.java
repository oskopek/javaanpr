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

    private String directory;
    private StringBuilder output; // TODO refactor into a form
    private boolean enabled;

    public ReportGenerator(String directory) throws IOException {
        this.directory = directory;
        this.enabled = true;
        File f = new File(directory);
        if (!f.exists() || !f.isDirectory()) {
            throw new IOException("Report directory '" + directory + "' doesn't exist or isn't a directory");
        }
        this.output = new StringBuilder();
        this.output.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" + "<html>"
                + "<head><title>ANPR report</title>" + "</head>" + "<style type=\"text/css\">"
                + "@import \"style.css\";" + "</style>");
    }

    public ReportGenerator() {
        this.enabled = false;
    }

    public void insertText(String text) {
        if (!this.enabled) {
            return;
        }
        this.output.append(text + "\n");
    }

    public void insertImage(BufferedImage image, String cls, int w, int h)
            throws IllegalArgumentException, IOException {
        if (!this.enabled) {
            return;
        }
        String imageName = String.valueOf(image.hashCode()) + ".jpg";
        this.saveImage(image, imageName);
        if ((w != 0) && (h != 0)) {
            this.output.append("<img src='" + imageName + "' alt='' width='" + w + "' height='" + h + "' class='" + cls
                    + "'>\n");
        } else {
            this.output.append("<img src='" + imageName + "' alt='' class='" + cls + "'>\n");
        }
    }

    public void finish() throws IOException {
        if (!this.enabled) {
            return;
        }
        this.output.append("</html>");
        FileOutputStream os = new FileOutputStream(this.directory + File.separator + "index.html");
        Writer writer = new OutputStreamWriter(os);
        writer.write(this.output.toString());
        writer.flush();
        writer.close();
        String cssPath = Configurator.getConfigurator().getPathProperty("reportgeneratorcss");
        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(cssPath);
        this.saveStreamToFile(inStream, new File(this.directory + File.separator + "style.css"));
    }

    public void saveStreamToFile(InputStream inStream, File out) throws IOException {
        FileOutputStream outStream = new FileOutputStream(out);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inStream.read(bytes)) != -1) {
            outStream.write(bytes, 0, read);
        }
        outStream.close();
        inStream.close();
    }

    public void saveImage(BufferedImage bi, String filename) throws IOException, IllegalArgumentException {
        if (!this.enabled) {
            return;
        }
        String type = new String(filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase());
        if (!type.equals("bmp") && !type.equals("jpg") && !type.equals("jpeg") && !type.equals("png")) {
            throw new IllegalArgumentException("Unsupported file format");
        }
        File destination = new File(this.directory + File.separator + filename);
        try {
            ImageIO.write(bi, type, destination);
        } catch (IOException e) {
            throw new IOException("Can't open destination report directory", e);
        }
    }
}
