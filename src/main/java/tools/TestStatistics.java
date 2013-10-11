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
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package tools;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileReader;
//import java.io.IOException;
import java.util.Vector;

public class TestStatistics {

	public static String helpText = ""
			+ "-----------------------------------------------------------\n"
			+ "ANPR Statistics Generator\n"
			+ "Copyright (c) Ondrej Martinsky, 2006-2007\n" + "\n"
			+ "Licensed under the Educational Community License,\n" + "\n"
			+ "Command line arguments\n" + "\n"
			+ "    -help         Displays this help\n"
			+ "    -i <file>     Create statistics for test file\n" + "\n"
			+ "Test file must be have a CSV format\n"
			+ "Each row must contain name of analysed snapshot,\n"
			+ "real plate and recognized plate string\n" + "Example : \n"
			+ "001.jpg, 1B01234, 1B012??";

	public TestStatistics() {
	}

	public static void main(String[] args) {
		if ((args.length == 2) && args[0].equals("-i")) { // proceed analysis
			try {
				File f = new File(args[1]);
				BufferedReader input = new BufferedReader(new FileReader(f));
				String line;
				int lineCount = 0;
				String[] split;
				TestReport testReport = new TestReport();
				while ((line = input.readLine()) != null) {
					lineCount++;
					split = line.split(",", 4);
					if (split.length != 3) {
						System.out.println("Warning: line " + lineCount
								+ " contains invalid CSV data (skipping)");
						continue;
					}
					testReport.addRecord(testReport.new TestRecord(split[0],
							split[1], split[2]));
				}
				input.close();
				testReport.printStatistics();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		} else {
			// DONE display help
			System.out.println(TestStatistics.helpText);
		}

	}
}

class TestReport {
	class TestRecord {
		String name, plate, recognizedPlate;
		int good;
		int length;

		TestRecord(String name, String plate, String recognizedPlate) {
			this.name = name.trim();
			this.plate = plate.trim();
			this.recognizedPlate = recognizedPlate.trim();
			compute();
		}

		private void compute() {
			length = Math.max(plate.length(), recognizedPlate.length());
			int g1 = 0;
			int g2 = 0;
			for (int i = 0; i < length; i++) { // POROVNAVAT ODPREDU (napr.
												// BA123AB vs. BA123ABX)
				if (getChar(plate, i) == getChar(recognizedPlate, i)) {
					g1++;
				}
			}
			for (int i = 0; i < length; i++) { // POROVNAVAT ODZADU (napr.
												// BA123AB vs. XBA123AB)
				if (getChar(plate, length - i - 1) == getChar(recognizedPlate,
						length - i - 1)) {
					g2++;
				}
			}
			good = Math.max(g1, g2);
		}

		private char getChar(String string, int position) {
			if (position >= string.length()) {
				return ' ';
			}
			if (position < 0) {
				return ' ';
			}
			return string.charAt(position);
		}

		public int getGoodCount() {
			return good;
		}

		public int getLength() {
			return length;
		}

		public boolean isOk() {
			if (length != good) {
				return false;
			} else {
				return true;
			}
		}
	}

	Vector<TestRecord> records;

	TestReport() {
		records = new Vector<TestRecord>();
	}

	void addRecord(TestRecord testRecord) {
		records.add(testRecord);
	}

	void printStatistics() {
		int weightedScoreCount = 0;
		int binaryScoreCount = 0;
		int characterCount = 0;
		System.out.println("----------------------------------------------");
		System.out.println("Defective plates\n");

		for (TestRecord record : records) {
			characterCount += record.getLength();
			weightedScoreCount += record.getGoodCount();
			binaryScoreCount += (record.isOk() ? 1 : 0);
			if (!record.isOk()) {
				System.out
						.println(record.plate
								+ " ~ "
								+ record.recognizedPlate
								+ " ("
								+ (((float) record.getGoodCount() / record
										.getLength()) * 100) + "% ok)");
			}
		}
		System.out.println("\n----------------------------------------------");
		System.out.println("Test report statistics\n");
		System.out.println("Total number of plates     : " + records.size());
		System.out.println("Total number of characters : " + characterCount);
		System.out.println("Binary score               : "
				+ (((float) binaryScoreCount / records.size()) * 100));
		System.out.println("Weighted score             : "
				+ (((float) weightedScoreCount / characterCount) * 100));
	}
}
