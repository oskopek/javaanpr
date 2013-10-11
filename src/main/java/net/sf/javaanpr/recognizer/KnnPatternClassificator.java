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

package net.sf.javaanpr.recognizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.intelligence.Intelligence;

public class KnnPatternClassificator extends CharacterRecognizer {
	Vector<Vector<Double>> learnVectors;

	public KnnPatternClassificator() throws IOException {		
		String path = Intelligence.configurator
				.getPathProperty("char_learnAlphabetPath");
		
		String alphaString = "0123456789abcdefghijklmnopqrstuvwxyz";

		// inicializacia vektora na pozadovanu velkost (nulovanie poloziek)
		learnVectors = new Vector<Vector<Double>>();
		for (int i = 0; i < alphaString.length(); i++) {
			learnVectors.add(null);
		}
		
		ArrayList<String> filenames = new ArrayList<>();
		
		String footer = path.substring(path.indexOf('_'));
		
		String s;
		for(int i = 0; i < alphaString.length(); i++) {
		    s = alphaString.charAt(i) + footer + ".jpg";
		    
		    filenames.add(s);
		}
		
		for (String fileName : filenames) {
			int alphaPosition = alphaString.indexOf(fileName.toLowerCase()
					.charAt(0));
			if (alphaPosition == -1) {
				continue; // je to nezname meno suboru, skip
			}

			Char imgChar = new Char(path + File.separator + fileName);
			imgChar.normalize();
			// zapis na danu poziciu vo vektore
			learnVectors.set(alphaPosition, imgChar.extractFeatures());
		}

		// kontrola poloziek vektora
		for (int i = 0; i < alphaString.length(); i++) {
			if (learnVectors.elementAt(i) == null) {
				throw new IOException("Warning : alphabet in " + path
						+ " is not complete");
			}
		}

	}

	@Override
	public RecognizedChar recognize(Char chr) throws Exception {
		Vector<Double> tested = chr.extractFeatures();
		//int minx = 0;
		//float minfx = Float.POSITIVE_INFINITY;

		RecognizedChar recognized = new RecognizedChar();

		for (int x = 0; x < learnVectors.size(); x++) {
			// pre lepsie fungovanie bol pouhy rozdiel vektorov nahradeny
			// euklidovskou vzdialenostou
			float fx = simplifiedEuclideanDistance(tested,
					learnVectors.elementAt(x));

			recognized.addPattern(recognized.new RecognizedPattern(alphabet[x],
					fx));

			// if (fx < minfx) {
			// minfx = fx;
			// minx = x;
			// }
		}
		// return new RecognizedChar(this.alphabet[minx], minfx);
		recognized.sort(0);
		return recognized;
	}

	public float difference(Vector<Double> vectorA, Vector<Double> vectorB) {
		float diff = 0;
		for (int x = 0; x < vectorA.size(); x++) {
			diff += Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
		}
		return diff;
	}

	public float simplifiedEuclideanDistance(Vector<Double> vectorA,
			Vector<Double> vectorB) {
		float diff = 0;
		float partialDiff;
		for (int x = 0; x < vectorA.size(); x++) {
			partialDiff = (float) Math.abs(vectorA.elementAt(x)
					- vectorB.elementAt(x));
			diff += partialDiff * partialDiff;
		}
		return diff;
	}

}
