/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://net.sf.javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://net.sf.javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package net.sf.javaanpr.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import net.sf.javaanpr.intelligence.Intelligence;

public class ReportGenerator {
	private String path;
	private String output;
	//private BufferedWriter out;
	private boolean enabled;

	public ReportGenerator(String path) throws IOException {
		this.path = path;
		enabled = true;

		File f = new File(path);
		if (!f.exists()) {
			throw new IOException("Report directory '" + path
					+ "' doesn't exists");
		}

		output = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
				+ "<html>"
				+ "<head><title>ANPR report</title>"
				+ "</head>"
				+ "<style type=\"text/css\">"
				+ "@import \"style.css\";"
				+ "</style>";

	}

	public ReportGenerator() {
		enabled = false;
	}

	public void insertText(String text) {
		if (!enabled) {
			return;
		}
		output += text;
		output += "\n";
	}

	public void insertImage(BufferedImage image, String cls, int w, int h)
			throws IOException {
		if (!enabled) {
			return;
		}
		String imageName = String.valueOf(image.hashCode()) + ".jpg";
		saveImage(image, path + File.separator + imageName);
		if ((w != 0) && (h != 0)) {
			output += "<img src='" + imageName + "' alt='' width='" + w
					+ "' height='" + h + "' class='" + cls + "'>\n";
		} else {
			output += "<img src='" + imageName + "' alt='' class='" + cls
					+ "'>\n";
		}
	}

	public void finish() throws Exception {
		if (!enabled) {
			return;
		}
		output += "</html>";
		FileOutputStream os = new FileOutputStream(path + File.separator
				+ "index.html");
		Writer writer = new OutputStreamWriter(os);
		writer.write(output);
		writer.flush();
		writer.close();
		copyFile(
				new File(
						Intelligence.configurator
								.getPathProperty("reportgeneratorcss")),
				new File(path + File.separator + "style.css"));
	}

	public void copyFile(File in, File out) throws Exception {
		FileInputStream inStream = new FileInputStream(in);
		FileOutputStream outStream = new FileOutputStream(out);
		
		FileChannel sourceChannel = inStream.getChannel();
		FileChannel destinationChannel = outStream.getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		// or
		// destinationChannel.transferFrom
		// (sourceChannel, 0, sourceChannel.size());
		sourceChannel.close();
		destinationChannel.close();
		outStream.close();
		inStream.close();
	}

	public void saveImage(BufferedImage bi, String filepath) throws IOException {
		if (!enabled) {
			return;
		}
		String type = new String(filepath.substring(
				filepath.lastIndexOf('.') + 1, filepath.length()).toUpperCase());
		if (!type.equals("BMP") && !type.equals("JPG") && !type.equals("JPEG")
				&& !type.equals("PNG")) {
			throw new IOException("Unsupported file format");
		}

		File destination = new File(filepath);
		try {
			ImageIO.write(bi, type, destination);
		} catch (Exception e) {
			System.out.println("catched " + e.toString());
			System.exit(1);
			throw new IOException("Can't open destination report directory");
		}
	}
}
