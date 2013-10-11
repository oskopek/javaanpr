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
for more informations about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package javaanpr.recognizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javaanpr.imageanalysis.Char;

public abstract class CharacterRecognizer {

	// rozpoznavane pismena :
	// 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27
	// 28 29 30 31 32 33 34 35
	// 0 1 2 3 4 5 6 7 8 9 A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
	public static char[] alphabet = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
			'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z' };

	public static float[][] features = { { 0, 1, 0, 1 }, // 0
			{ 1, 0, 1, 0 }, // 1
			{ 0, 0, 1, 1 }, // 2
			{ 1, 1, 0, 0 }, // 3
			{ 0, 0, 0, 1 }, // 4
			{ 1, 0, 0, 0 }, // 5
			{ 1, 1, 1, 0 }, // 6
			{ 0, 1, 1, 1 }, // 7
			{ 0, 0, 1, 0 }, // 8
			{ 0, 1, 0, 0 }, // 9
			{ 1, 0, 1, 1 }, // 10
			{ 1, 1, 0, 1 } // 11
	};

	public class RecognizedChar {
		public class RecognizedPattern {
			private char chr;
			private float cost;

			RecognizedPattern(char chr, float value) {
				this.chr = chr;
				cost = value;
			}

			public char getChar() {
				return chr;
			}

			public float getCost() {
				return cost;
			}
		}

		public class PatternComparer implements Comparator<Object> {
			int direction;

			public PatternComparer(int direction) {
				this.direction = direction;
			}

			@Override
			public int compare(Object o1, Object o2) {
				float cost1 = ((RecognizedPattern) o1).getCost();
				float cost2 = ((RecognizedPattern) o2).getCost();

				int ret = 0;

				if (cost1 < cost2) {
					ret = -1;
				}
				if (cost1 > cost2) {
					ret = 1;
				}
				if (direction == 1) {
					ret *= -1;
				}
				return ret;
			}
		}

		private Vector<RecognizedPattern> patterns;
		private boolean isSorted;

		RecognizedChar() {
			patterns = new Vector<RecognizedPattern>();
			isSorted = false;
		}

		public void addPattern(RecognizedPattern pattern) {
			patterns.add(pattern);
		}

		public boolean isSorted() {
			return isSorted;
		}

		public void sort(int direction) {
			if (isSorted) {
				return;
			}
			isSorted = true;
			Collections.sort(patterns, new PatternComparer(direction));
		}

		public Vector<RecognizedPattern> getPatterns() {
			if (isSorted) {
				return patterns;
			}
			return null; // if not sorted
		}

		public RecognizedPattern getPattern(int i) {
			if (isSorted) {
				return patterns.elementAt(i);
			}
			return null;
		}

		public BufferedImage render() {
			int width = 500;
			int height = 200;
			BufferedImage histogram = new BufferedImage(width + 20,
					height + 20, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphic = histogram.createGraphics();

			graphic.setColor(Color.LIGHT_GRAY);
			Rectangle backRect = new Rectangle(0, 0, width + 20, height + 20);
			graphic.fill(backRect);
			graphic.draw(backRect);

			graphic.setColor(Color.BLACK);

			int colWidth = width / patterns.size();
			int left, top;

			for (int ay = 0; ay <= 100; ay += 10) {
				int y = 15 + (int) (((100 - ay) / 100.0f) * (height - 20));
				graphic.drawString(new Integer(ay).toString(), 3, y + 11);
				graphic.drawLine(25, y + 5, 35, y + 5);
			}
			graphic.drawLine(35, 19, 35, height);

			graphic.setColor(Color.BLUE);

			for (int i = 0; i < patterns.size(); i++) {
				left = (i * colWidth) + 42;
				top = height
						- (int) (patterns.elementAt(i).cost * (height - 20));

				graphic.drawRect(left, top, colWidth - 2, height - top);
				graphic.drawString(patterns.elementAt(i).chr + " ", left + 2,
						top - 8);
			}
			return histogram;
		}
	}

	/** Creates a new instance of CharacterRecognizer */
	public CharacterRecognizer() {
	}

	public abstract RecognizedChar recognize(Char chr) throws Exception;
}
