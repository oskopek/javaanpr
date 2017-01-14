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

package net.sf.javaanpr.intelligence.parser;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.intelligence.RecognizedPlate;
import net.sf.javaanpr.intelligence.SyntaxAnalysisMode;
import net.sf.javaanpr.jar.Main;
import net.sf.javaanpr.recognizer.RecognizedChar;
import net.sf.javaanpr.recognizer.RecognizedPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static final transient Logger logger = LoggerFactory.getLogger(Parser.class);
    private List<PlateForm> plateForms;

    /**
     * Creates a new instance of Parser.
     *
     * @throws ParserConfigurationException a ParserConfigurationException
     * @throws SAXException a SAXException
     * @throws IOException an IOException
     */
    public Parser() throws ParserConfigurationException, SAXException, IOException { // TODO javadoc
        plateForms = new ArrayList<>();
        String fileName = Configurator.getConfigurator().getPathProperty("intelligence_syntaxDescriptionFile");
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Failed to get syntax description file from Configurator");
        }
        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(fileName);
        if (inStream == null) {
            throw new IOException("Couldn't find parser syntax description file");
        }
        try {
            plateForms = loadFromXml(inStream);
        } catch (ParserConfigurationException | SAXException | IOException e) { // TODO fix
            logger.error("Failed to load from parser syntax description file");
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
    public List<PlateForm> loadFromXml(String fileName)
            throws ParserConfigurationException, SAXException, IOException { // TODO javadoc
        InputStream inStream = Configurator.getConfigurator().getResourceAsStream(fileName);
        return loadFromXml(inStream);
    }

    /**
     * @param inStream input stream from the xml file
     * @return {@link List} of loaded {@link PlateForm}s
     * @throws ParserConfigurationException a ParserConfigurationException
     * @throws SAXException a SAXException
     * @throws IOException an IOException
     */
    public List<PlateForm> loadFromXml(InputStream inStream)
            throws ParserConfigurationException, SAXException, IOException {  // TODO javadoc
        List<PlateForm> plateForms = new ArrayList<>();
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
                form.addPosition(new Position(content.toUpperCase()));
            }
            plateForms.add(form);
        }
        return plateForms;
    }

    public void unFlagAll() {
        for (PlateForm form : plateForms) {
            form.setFlagged(false);
        }
    }

    /**
     * For given {@code length}, finds a {@link PlateForm} of the same length. If
     * no such {@link PlateForm} is found, tries to find one with less characters.
     *
     * @param length the number of characters of the PlateForm
     */
    public void flagEqualOrShorterLength(int length) {
        boolean found = false;
        for (int i = length; (i >= 1) && !found; i--) {
            for (PlateForm form : plateForms) {
                if (form.length() == i) {
                    form.setFlagged(true);
                    found = true;
                }
            }
        }
    }

    public void flagEqualLength(int length) {
        for (PlateForm form : plateForms) {
            if (form.length() == length) {
                form.setFlagged(true);
            }
        }
    }

    public void invertFlags() {
        for (PlateForm form : plateForms) {
            form.setFlagged(!form.isFlagged());
        }
    }

    /**
     * Syntactically parses text from the given {@link RecognizedPlate} in the specified analysis mode.
     * <p>
     *
     * @param recognizedPlate the plate to parse
     * @param syntaxAnalysisMode the mode in which to parse
     * @return the parsed recognized plate text
     */
    public String parse(RecognizedPlate recognizedPlate, SyntaxAnalysisMode syntaxAnalysisMode) {
        int length = recognizedPlate.getChars().size();

        switch (syntaxAnalysisMode) {
            case DO_NOT_PARSE:
                Main.rg.insertText(
                        " result : " + recognizedPlate.getString() + " --> <font size=15>" + recognizedPlate.getString()
                                + "</font><hr><br>");
                return recognizedPlate.getString();
            case ONLY_EQUAL_LENGTH:
                unFlagAll();
                flagEqualLength(length);
                break;
            case EQUAL_OR_SHORTER_LENGTH:
                unFlagAll();
                flagEqualOrShorterLength(length);
                break;
            default:
                throw new IllegalArgumentException("Expected SyntaxAnalysisMode.DO_NOT_PARSE, "
                        + "SyntaxAnalysisMode.ONLY_EQUAL_LENGTH or SyntaxAnalysisMode.EQUAL_OR_SHORTER_LENGTH.");
        }

        List<FinalPlate> finalPlates = new ArrayList<>();

        for (PlateForm form : plateForms) {
            if (!form.isFlagged()) {
                continue;
            }
            for (int i = 0; i <= (length - form.length()); i++) { // moving the form on the plate
                logger.debug("Comparing {} with form {} and offset {}.", recognizedPlate, form.getName(), i);
                FinalPlate finalPlate = new FinalPlate();
                for (int j = 0; j < form.length(); j++) { // all chars of the form
                    RecognizedChar rc = recognizedPlate.getChar(j + i);
                    if (form.getPosition(j).isAllowed(rc.getPattern(0).getChar())) {
                        finalPlate.addChar(rc.getPattern(0).getChar());
                    } else { // a swap needed
                        finalPlate.requiredChanges++; // +1 for every char
                        for (int x = 0; x < rc.getPatterns().size(); x++) {
                            if (form.getPosition(j).isAllowed(rc.getPattern(x).getChar())) {
                                RecognizedPattern rp = rc.getPattern(x);
                                finalPlate.requiredChanges += (rp.getCost() / 100); // +x for its cost
                                finalPlate.addChar(rp.getChar());
                                break;
                            }
                        }
                    }
                }
                logger.debug("Adding {} with required changes {}.", finalPlate.plate, finalPlate.requiredChanges);
                finalPlates.add(finalPlate);
            }
        }
        if (finalPlates.size() == 0) {
            return recognizedPlate.getString();
        }
        // else: find the plate with lowest number of swaps
        float minimalChanges = Float.POSITIVE_INFINITY;
        int minimalIndex = 0;
        for (int i = 0; i < finalPlates.size(); i++) {
            logger.debug("Plate {} : {} with required changes {}.", i, finalPlates.get(i).plate,
                    finalPlates.get(i).requiredChanges);
            if (finalPlates.get(i).requiredChanges <= minimalChanges) {
                minimalChanges = finalPlates.get(i).requiredChanges;
                minimalIndex = i;
            }
        }
        String toReturn = recognizedPlate.getString();
        if (finalPlates.get(minimalIndex).requiredChanges <= 2) {
            toReturn = finalPlates.get(minimalIndex).plate;
        }
        return toReturn;
    }

    private static final class FinalPlate {
        private String plate;
        private float requiredChanges = 0;

        private FinalPlate() {
            plate = "";
        }

        public void addChar(char chr) {
            plate = plate + chr;
        }
    }
}
