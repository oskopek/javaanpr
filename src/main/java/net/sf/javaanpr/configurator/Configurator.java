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

package net.sf.javaanpr.configurator;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Configurator {

    private static Configurator configurator;

    /* Default name of configuration file */
    private String fileName = "config.xml";
    /* Configuration file's comment */
    private String comment = "This is the global configuration file for Automatic Number Plate Recognition System";

    /* Primary property list containing values from configuration file */
    private Properties list;

    public Configurator() throws IOException {
        this.list = new Properties();
        /* ***** BEGIN *** Definition of property defaults ******* */

        // PHOTO

        // adaptive thresholding radius (0 = no adaptive)
        this.setIntProperty("photo_adaptivethresholdingradius", 7); // 7 is
        // recommanded

        // BANDGRAPH - spracovanie horizontalnej projekcie detekovanej oblasti
        // znacky
        // na ose X sa detekuje peak, peakfoot, a nakoniec sa to nasobi p.d.m.c
        // konstantou
        this.setDoubleProperty("bandgraph_peakfootconstant", 0.55); // 0.75
        this.setDoubleProperty("bandgraph_peakDiffMultiplicationConstant", 0.2);

        // CARSNAPSHOT
        this.setIntProperty("carsnapshot_distributormargins", 25);
        this.setIntProperty("carsnapshot_graphrankfilter", 9);

        // CARSNAPSHOTGRAPH
        this.setDoubleProperty("carsnapshotgraph_peakfootconstant", 0.55); // 0.55
        this.setDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant", 0.1);

        this.setIntProperty("intelligence_skewdetection", 0);

        // CHAR
        // this.setDoubleProperty("char_contrastnormalizationconstant", 0.5);
        // //1.0
        this.setIntProperty("char_normalizeddimensions_x", 8); // 8
        this.setIntProperty("char_normalizeddimensions_y", 13); // 13
        this.setIntProperty("char_resizeMethod", 1); // 0=linear 1=average
        this.setIntProperty("char_featuresExtractionMethod", 0); // 0=map,
        // 1=edge
        this.setStrProperty("char_neuralNetworkPath", "/neuralnetworks/network_avgres_813_map.xml");
        this.setStrProperty("char_learnAlphabetPath", "/alphabets/alphabet_8x13");
        this.setIntProperty("intelligence_classification_method", 0); // 0 =
                                                                      // pattern
                                                                      // match
                                                                      // ,1=nn

        // PLATEGRAPH
        this.setDoubleProperty("plategraph_peakfootconstant", 0.7); // urci
        // sirku
        // detekovanej
        // medzery
        this.setDoubleProperty("plategraph_rel_minpeaksize", 0.86); // 0.85 //
        // mensie
        // cislo
        // seka
        // znaky,
        // vacsie
        // zase
        // nespravne
        // zdruzuje

        // PLATEGRAPHHORIZONTALGRAPH
        this.setDoubleProperty("platehorizontalgraph_peakfootconstant", 0.05);
        this.setIntProperty("platehorizontalgraph_detectionType", 1); // 1=edgedetection
                                                                      // 0=magnitudederivate

        // PLATEVERICALGRAPH
        this.setDoubleProperty("plateverticalgraph_peakfootconstant", 0.42);

        // INTELLIGENCE
        this.setIntProperty("intelligence_numberOfBands", 3);
        this.setIntProperty("intelligence_numberOfPlates", 3);
        this.setIntProperty("intelligence_numberOfChars", 20);

        this.setIntProperty("intelligence_minimumChars", 5);
        this.setIntProperty("intelligence_maximumChars", 15);

        // plate heuristics
        this.setDoubleProperty("intelligence_maxCharWidthDispersion", 0.5); // in
        // plate
        this.setDoubleProperty("intelligence_minPlateWidthHeightRatio", 0.5);
        this.setDoubleProperty("intelligence_maxPlateWidthHeightRatio", 15.0);

        // char heuristics
        this.setDoubleProperty("intelligence_minCharWidthHeightRatio", 0.1);
        this.setDoubleProperty("intelligence_maxCharWidthHeightRatio", 0.92);
        this.setDoubleProperty("intelligence_maxBrightnessCostDispersion", 0.161);
        this.setDoubleProperty("intelligence_maxContrastCostDispersion", 0.1);
        this.setDoubleProperty("intelligence_maxHueCostDispersion", 0.145);
        this.setDoubleProperty("intelligence_maxSaturationCostDispersion", 0.24); // 0.15
        this.setDoubleProperty("intelligence_maxHeightCostDispersion", 0.2);
        this.setDoubleProperty("intelligence_maxSimilarityCostDispersion", 100);

        // RECOGNITION
        this.setIntProperty("intelligence_syntaxanalysis", 2);
        this.setStrProperty("intelligence_syntaxDescriptionFile", "/syntax/syntax.xml");

        // NEURAL NETWORK
        // int maxK, double eps, double lambda, double micro

        this.setIntProperty("neural_maxk", 8000); // maximum K - maximalny pocet
                                                  // iteracii
        this.setDoubleProperty("neural_eps", 0.07); // epsilon - pozadovana
        // presnost
        this.setDoubleProperty("neural_lambda", 0.05); // lambda factor -
                                                       // rychlost ucenia,
                                                       // velkost gradientu
        this.setDoubleProperty("neural_micro", 0.5); // micro - momentovy clen
        // pre prekonavanie
        // lokalnych extremov
        // top(log(m recognized units)) = 6
        this.setIntProperty("neural_topology", 20); // topologia strednej vrstvy

        /* ***** END ***** Definition of property defaults ******* */

        this.setStrProperty("help_file_help", "/help/help.html");
        this.setStrProperty("help_file_about", "/help/about.html");
        this.setStrProperty("reportgeneratorcss", "/reportgenerator/style.css");

        InputStream is = this.getResourceAsStream(this.fileName);

        if (is != null) {
            this.loadConfiguration(is);
            is.close();
        }

        Configurator.configurator = this;
    }

    public void setConfigurationFileName(String name) {
        this.fileName = name;
    }

    public String getConfigurationFileName() {
        return this.fileName;
    }

    public String getStrProperty(String name) {
        return this.list.getProperty(name).toString();
    }

    public String getPathProperty(String name) {
        return this.getStrProperty(name).replace('/', File.separatorChar);

    }

    public void setStrProperty(String name, String value) {
        this.list.setProperty(name, value);
    }

    public int getIntProperty(String name) throws NumberFormatException {
        return Integer.decode(this.list.getProperty(name));
    }

    public void setIntProperty(String name, int value) {
        this.list.setProperty(name, String.valueOf(value));
    }

    public double getDoubleProperty(String name) throws NumberFormatException {
        return Double.parseDouble(this.list.getProperty(name));
    }

    public void setDoubleProperty(String name, double value) {
        this.list.setProperty(name, String.valueOf(value));
    }

    public Color getColorProperty(String name) {
        return new Color(Integer.decode(this.list.getProperty(name)));
    }

    public void setColorProperty(String name, Color value) {
        this.list.setProperty(name, String.valueOf(value.getRGB()));
    }

    public void saveConfiguration() throws IOException {
        this.saveConfiguration(this.fileName);
    }

    public void saveConfiguration(String arg_file) throws IOException {
        FileOutputStream os = new FileOutputStream(arg_file);
        this.list.storeToXML(os, this.comment);
    }

    public void loadConfiguration() throws InvalidPropertiesFormatException, IOException {
        this.loadConfiguration(this.fileName);
    }

    public void loadConfiguration(String arg_file) throws InvalidPropertiesFormatException, IOException {
        InputStream is = this.getResourceAsStream(arg_file);

        this.loadConfiguration(is);
    }

    public void loadConfiguration(InputStream arg_stream) throws InvalidPropertiesFormatException, IOException {
        if (arg_stream == null) {
            this.list = null;
            return;
        }

        this.list.loadFromXML(arg_stream);
    }

    public InputStream getResourceAsStream(String filename) {
        String corrected = filename;

        URL f = this.getClass().getResource(corrected);
        if (f != null) {
            return this.getClass().getResourceAsStream(corrected);
        }

        if (filename.startsWith("/")) {
            corrected = filename.substring(1);
        } else if (filename.startsWith("./")) {
            corrected = filename.substring(2);
        } else {
            corrected = "/" + filename;
        }

        f = this.getClass().getResource(corrected);

        if (f != null) {
            return this.getClass().getResourceAsStream(corrected);
        }

        /*
         * Should actually load filename. It is here for the GUI. Loading images exactly from specified filesystem path
         */
        File file = new File(filename);
        if (file.exists()) {
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return fis;
        }

        return null;
    }

    public static Configurator getConfigurator() {
        if (configurator == null) {
            try {
                configurator = new Configurator();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return configurator;
    }

}