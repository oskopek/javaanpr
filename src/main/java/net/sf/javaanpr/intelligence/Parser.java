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

package net.sf.javaanpr.intelligence;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.jar.Main;
import net.sf.javaanpr.recognizer.CharacterRecognizer.RecognizedChar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class Parser {
    public class PlateForm {
        public class Position {
            public char[] allowedChars;

            public Position(String data) {
                this.allowedChars = data.toCharArray();
            }

            public boolean isAllowed(char chr) {
                boolean ret = false;
                for (int i = 0; i < this.allowedChars.length; i++) {
                    if (this.allowedChars[i] == chr) {
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
            this.positions = new Vector<Position>();
        }

        public void addPosition(Position p) {
            this.positions.add(p);
        }

        public Position getPosition(int index) {
            return this.positions.elementAt(index);
        }

        public int length() {
            return this.positions.size();
        }

    }

    public class FinalPlate {
        public String plate;
        public float requiredChanges = 0;

        FinalPlate() {
            this.plate = "";
        }

        public void addChar(char chr) {
            this.plate = this.plate + chr;
        }
    }

    Vector<PlateForm> plateForms;

    /**
     * Creates a new instance of Parser.
     *
     * @throws ParserConfigurationException a ParserConfigurationException
     * @throws SAXException a SAXException
     * @throws IOException an IOException
     */
    public Parser() throws ParserConfigurationException, SAXException, IOException { // TODO javadoc
        this.plateForms = new Vector<PlateForm>();

        String fileName = Configurator.getConfigurator().getPathProperty("intelligence_syntaxDescriptionFile");

        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Failed to get syntax description file from Configurator");
        }

        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(fileName);

        if (inStream == null) {
            throw new IOException("Couldn't find parser syntax description file");
        }

        try {
            this.plateForms = this.loadFromXml(inStream);
        } catch (ParserConfigurationException e) { // TODO fix this
            System.err.println("Failed to load from parser syntax description file");
            throw e;
        } catch (SAXException e) {
            System.err.println("Failed to load from parser syntax description file");
            throw e;
        } catch (IOException e) {
            System.err.println("Failed to load from parser syntax description file");
            throw e;
        }
    }

    /**
     * @param fileName the path to the xml file
     * @return null if couldn't load file
     * @throws ParserConfigurationException a ParserConfigurationException
     * @throws SAXException a SAXException
     * @throws IOException an IOException
     * @deprecated use {@link Parser#loadFromXml(InputStream)}
     */
    @Deprecated
    public Vector<PlateForm> loadFromXml(String fileName) throws ParserConfigurationException, SAXException,
            IOException { // TODO javadoc
        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(fileName);
        return this.loadFromXml(inStream);
    }

    /**
     * @param inStream input stream from the xml file
     * @return {@link Vector} of loaded {@link PlateForm}s
     * @throws ParserConfigurationException a ParserConfigurationException
     * @throws SAXException a SAXException
     * @throws IOException an IOException
     */
    public Vector<PlateForm> loadFromXml(InputStream inStream) throws ParserConfigurationException, SAXException,
            IOException {  // TODO javadoc
        Vector<PlateForm> plateForms = new Vector<PlateForm>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.parse(inStream);

        Node structureNode = doc.getDocumentElement();
        NodeList structureNodeContent = structureNode.getChildNodes();
        for (int i = 0; i < structureNodeContent.getLength(); i++) {
            Node typeNode = structureNodeContent.item(i);
            if (!typeNode.getNodeName().equals("type")) {
                continue;
            }
            PlateForm form = new PlateForm(((Element) typeNode).getAttribute("name"));
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
        for (PlateForm form : this.plateForms) {
            form.flagged = false;
        }
    }

    // pre danu dlzku znacky sa pokusi najst nejaky plateform o rovnakej dlzke
    // v pripade ze nenajde ziadny, pripusti moznost ze je nejaky znak navyse
    // a hlada plateform s mensim poctom pismen
    public void flagEqualOrShorterLength(int length) {
        boolean found = false;
        for (int i = length; (i >= 1) && !found; i--) {
            for (PlateForm form : this.plateForms) {
                if (form.length() == i) {
                    form.flagged = true;
                    found = true;
                }
            }
        }
    }

    public void flagEqualLength(int length) {
        for (PlateForm form : this.plateForms) {
            if (form.length() == length) {
                form.flagged = true;
            }
        }
    }

    public void invertFlags() {
        for (PlateForm form : this.plateForms) {
            form.flagged = !form.flagged;
        }
    }

    // syntax analysis mode : 0 (do not parse)
    // : 1 (only equal length)
    // : 2 (equal or shorter)
    public String parse(RecognizedPlate recognizedPlate, int syntaxAnalysisMode) {
        if (syntaxAnalysisMode == 0) {
            Main.rg.insertText(" result : " + recognizedPlate.getString() + " --> <font size=15>"
                    + recognizedPlate.getString() + "</font><hr><br>");
            return recognizedPlate.getString();
        }

        int length = recognizedPlate.chars.size();
        this.unFlagAll();
        if (syntaxAnalysisMode == 1) {
            this.flagEqualLength(length);
        } else {
            this.flagEqualOrShorterLength(length);
        }

        Vector<FinalPlate> finalPlates = new Vector<FinalPlate>();

        for (PlateForm form : this.plateForms) {
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

                    if (form.getPosition(ii).isAllowed(rc.getPattern(0).getChar())) {
                        finalPlate.addChar(rc.getPattern(0).getChar());
                    } else { // treba vymenu
                        finalPlate.requiredChanges++; // +1 za pismeno
                        for (int x = 0; x < rc.getPatterns().size(); x++) {
                            if (form.getPosition(ii).isAllowed(rc.getPattern(x).getChar())) {
                                RecognizedChar.RecognizedPattern rp = rc.getPattern(x);
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
