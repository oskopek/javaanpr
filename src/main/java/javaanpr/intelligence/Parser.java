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

package javaanpr.intelligence;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;





//import org.xml.sax.SAXException;
import javaanpr.Main;
import javaanpr.configurator.Configurator;
import javaanpr.recognizer.CharacterRecognizer.RecognizedChar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



import javax.xml.parsers.ParserConfigurationException;


//import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parser {
	public class PlateForm {
		public class Position {
			public char[] allowedChars;

			public Position(String data) {
				allowedChars = data.toCharArray();
			}

			public boolean isAllowed(char chr) {
				boolean ret = false;
				for (int i = 0; i < allowedChars.length; i++) {
					if (allowedChars[i] == chr) {
						ret = true;
					}
				}
				return ret;
			}
		}

		Vector<Position> positions;
		String name;
		public boolean flagged = false;

		public PlateForm(String name) {
			this.name = name;
			positions = new Vector<Position>();
		}

		public void addPosition(Position p) {
			positions.add(p);
		}

		public Position getPosition(int index) {
			return positions.elementAt(index);
		}

		public int length() {
			return positions.size();
		}

	}

	public class FinalPlate {
		public String plate;
		public float requiredChanges = 0;

		FinalPlate() {
			plate = new String();
		}

		public void addChar(char chr) {
			plate = plate + chr;
		}
	}

	Vector<PlateForm> plateForms;

	/** Creates a new instance of Parser */
	public Parser() throws Exception {
		plateForms = new Vector<PlateForm>();
		
		String fileName = Intelligence.configurator
                .getPathProperty("intelligence_syntaxDescriptionFile");
		fileName = Configurator.getConfigurator().correctFilepath(fileName);
		
		plateForms = loadFromXml(getClass().getResourceAsStream(fileName));
	}

	public Vector<PlateForm> loadFromXml(String fileName) throws Exception {
		Vector<PlateForm> plateForms = new Vector<PlateForm>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(fileName);

		Node structureNode = doc.getDocumentElement();
		NodeList structureNodeContent = structureNode.getChildNodes();
		for (int i = 0; i < structureNodeContent.getLength(); i++) {
			Node typeNode = structureNodeContent.item(i);
			if (!typeNode.getNodeName().equals("type")) {
				continue;
			}
			PlateForm form = new PlateForm(
					((Element) typeNode).getAttribute("name"));
			NodeList typeNodeContent = typeNode.getChildNodes();
			for (int ii = 0; ii < typeNodeContent.getLength(); ii++) {
				Node charNode = typeNodeContent.item(ii);
				if (!charNode.getNodeName().equals("char")) {
					continue;
				}
				String content = ((Element) charNode).getAttribute("content");

				form.addPosition(form.new Position(content.toUpperCase()));
			}
			plateForms.add(form);
		}
		return plateForms;
	}
	
	public Vector<PlateForm> loadFromXml(InputStream fileName) throws ParserConfigurationException, SAXException, IOException {
        Vector<PlateForm> plateForms = new Vector<PlateForm>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.parse(fileName);

        Node structureNode = doc.getDocumentElement();
        NodeList structureNodeContent = structureNode.getChildNodes();
        for (int i = 0; i < structureNodeContent.getLength(); i++) {
            Node typeNode = structureNodeContent.item(i);
            if (!typeNode.getNodeName().equals("type")) {
                continue;
            }
            PlateForm form = new PlateForm(
                    ((Element) typeNode).getAttribute("name"));
            NodeList typeNodeContent = typeNode.getChildNodes();
            for (int ii = 0; ii < typeNodeContent.getLength(); ii++) {
                Node charNode = typeNodeContent.item(ii);
                if (!charNode.getNodeName().equals("char")) {
                    continue;
                }
                String content = ((Element) charNode).getAttribute("content");

                form.addPosition(form.new Position(content.toUpperCase()));
            }
            plateForms.add(form);
        }
        return plateForms;
    }

	// //
	public void unFlagAll() {
		for (PlateForm form : plateForms) {
			form.flagged = false;
		}
	}

	// pre danu dlzku znacky sa pokusi najst nejaky plateform o rovnakej dlzke
	// v pripade ze nenajde ziadny, pripusti moznost ze je nejaky znak navyse
	// a hlada plateform s mensim poctom pismen
	public void flagEqualOrShorterLength(int length) {
		boolean found = false;
		for (int i = length; (i >= 1) && !found; i--) {
			for (PlateForm form : plateForms) {
				if (form.length() == i) {
					form.flagged = true;
					found = true;
				}
			}
		}
	}

	public void flagEqualLength(int length) {
		for (PlateForm form : plateForms) {
			if (form.length() == length) {
				form.flagged = true;
			}
		}
	}

	public void invertFlags() {
		for (PlateForm form : plateForms) {
			form.flagged = !form.flagged;
		}
	}

	// syntax analysis mode : 0 (do not parse)
	// : 1 (only equal length)
	// : 2 (equal or shorter)
	public String parse(RecognizedPlate recognizedPlate, int syntaxAnalysisMode)
			throws IOException {
		if (syntaxAnalysisMode == 0) {
			Main.rg.insertText(" result : " + recognizedPlate.getString()
					+ " --> <font size=15>" + recognizedPlate.getString()
					+ "</font><hr><br>");
			return recognizedPlate.getString();
		}

		int length = recognizedPlate.chars.size();
		unFlagAll();
		if (syntaxAnalysisMode == 1) {
			flagEqualLength(length);
		} else {
			flagEqualOrShorterLength(length);
		}

		Vector<FinalPlate> finalPlates = new Vector<FinalPlate>();

		for (PlateForm form : plateForms) {
			if (!form.flagged) {
				continue; // skip unflagged
			}
			for (int i = 0; i <= (length - form.length()); i++) { // posuvanie
																	// formy po
																	// znacke
				// System.out.println("comparing "+recognizedPlate.getString()+" with form "+form.name+" and offset "+i
				// );
				FinalPlate finalPlate = new FinalPlate();
				for (int ii = 0; ii < form.length(); ii++) { // prebehnut vsetky
																// znaky formy
					// form.getPosition(ii).allowedChars // zoznam povolenych
					RecognizedChar rc = recognizedPlate.getChar(ii + i); // znak
																			// na
																			// znacke

					if (form.getPosition(ii).isAllowed(
							rc.getPattern(0).getChar())) {
						finalPlate.addChar(rc.getPattern(0).getChar());
					} else { // treba vymenu
						finalPlate.requiredChanges++; // +1 za pismeno
						for (int x = 0; x < rc.getPatterns().size(); x++) {
							if (form.getPosition(ii).isAllowed(
									rc.getPattern(x).getChar())) {
								RecognizedChar.RecognizedPattern rp = rc
										.getPattern(x);
								finalPlate.requiredChanges += (rp.getCost() / 100); // +x
																					// za
																					// jeho
																					// cost
								finalPlate.addChar(rp.getChar());
								break;
							}
						}
					}
				}
				// System.out.println("adding "+finalPlate.plate+" with required changes "+finalPlate.requiredChanges);
				finalPlates.add(finalPlate);
			}
		}
		//

		// tu este osetrit nespracovanie znacky v pripade ze nebola oznacena
		// ziadna
		if (finalPlates.size() == 0) {
			return recognizedPlate.getString();
		}
		// else :
		// najst tu s najmensim poctom vymen
		float minimalChanges = Float.POSITIVE_INFINITY;
		int minimalIndex = 0;
		// System.out.println("---");
		for (int i = 0; i < finalPlates.size(); i++) {
			// System.out.println("::"+finalPlates.elementAt(i).plate+" "+finalPlates.elementAt(i).requiredChanges);
			if (finalPlates.elementAt(i).requiredChanges <= minimalChanges) {
				minimalChanges = finalPlates.elementAt(i).requiredChanges;
				minimalIndex = i;
			}
		}

		String toReturn = recognizedPlate.getString();
		if (finalPlates.elementAt(minimalIndex).requiredChanges <= 2) {
			toReturn = finalPlates.elementAt(minimalIndex).plate;
		}
		return toReturn;
	}

}
