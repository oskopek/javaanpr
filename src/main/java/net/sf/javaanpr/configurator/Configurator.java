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

package net.sf.javaanpr.configurator;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public final class Configurator {

    private static Configurator configurator;

    /**
     * Default name of the configuration file.
     */
    private String fileName = "config.xml";

    /**
     * Primary property list containing values from the configuration file.
     */
    private Properties list;

    private Configurator() throws IOException {
        list = new Properties();

        setIntProperty("photo_adaptivethresholdingradius", 7); // 0 = not adaptive, 7 is recommended

        setDoubleProperty("bandgraph_peakfootconstant", 0.55); // 0.75
        setDoubleProperty("bandgraph_peakDiffMultiplicationConstant", 0.2);

        setIntProperty("carsnapshot_distributormargins", 25);
        setIntProperty("carsnapshot_graphrankfilter", 9);

        setDoubleProperty("carsnapshotgraph_peakfootconstant", 0.55); // 0.55
        setDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant", 0.1);

        setIntProperty("intelligence_skewdetection", 0);

        // setDoubleProperty("char_contrastnormalizationconstant", 0.5); //1.0
        setIntProperty("char_normalizeddimensions_x", 8); // 8
        setIntProperty("char_normalizeddimensions_y", 13); // 13
        setIntProperty("char_resizeMethod", 1); // 0 = linear 1 = average
        setIntProperty("char_featuresExtractionMethod", 0); // 0 = map, 1 = edge
        setStrProperty("char_neuralNetworkPath", "/neuralnetworks/network_avgres_813_map.xml");
        setStrProperty("char_learnAlphabetPath", "/alphabets/alphabet_8x13");
        setIntProperty("intelligence_classification_method", 0); // 0 = pattern match, 1 = neural network

        setDoubleProperty("plategraph_peakfootconstant", 0.7); // urci sirku detekovanej medzery
        setDoubleProperty("plategraph_rel_minpeaksize", 0.86); // 0.85 (mensie cislo seka znaky, vacsie zase
        // nespravne zdruzuje)

        setDoubleProperty("platehorizontalgraph_peakfootconstant", 0.05);
        setIntProperty("platehorizontalgraph_detectionType", 1); // 0 = magnitude derivate, 1 = edge detection

        setDoubleProperty("plateverticalgraph_peakfootconstant", 0.42);

        setIntProperty("intelligence_numberOfBands", 3);
        setIntProperty("intelligence_numberOfPlates", 3);
        setIntProperty("intelligence_numberOfChars", 20);
        setIntProperty("intelligence_minimumChars", 5);
        setIntProperty("intelligence_maximumChars", 15);
        // plate heuristics
        setDoubleProperty("intelligence_maxCharWidthDispersion", 0.5); // in plate
        setDoubleProperty("intelligence_minPlateWidthHeightRatio", 0.5);
        setDoubleProperty("intelligence_maxPlateWidthHeightRatio", 15.0);
        // char heuristics
        setDoubleProperty("intelligence_minCharWidthHeightRatio", 0.1);
        setDoubleProperty("intelligence_maxCharWidthHeightRatio", 0.92);
        setDoubleProperty("intelligence_maxBrightnessCostDispersion", 0.161);
        setDoubleProperty("intelligence_maxContrastCostDispersion", 0.1);
        setDoubleProperty("intelligence_maxHueCostDispersion", 0.145);
        setDoubleProperty("intelligence_maxSaturationCostDispersion", 0.24); // 0.15
        setDoubleProperty("intelligence_maxHeightCostDispersion", 0.2);
        setDoubleProperty("intelligence_maxSimilarityCostDispersion", 100);
        // recognition
        setIntProperty("intelligence_syntaxanalysis", 2);
        setStrProperty("intelligence_syntaxDescriptionFile", "/syntax.xml");

        // TODO: finish translation
        setIntProperty("neural_maxk", 8000); // maximum K - maximalny pocet iteracii
        setDoubleProperty("neural_eps", 0.07); // epsilon - pozadovana presnost
        setDoubleProperty("neural_lambda", 0.05); // lambda factor - rychlost ucenia, velkost gradientu
        setDoubleProperty("neural_micro", 0.5); // micro - momentovy clen pre prekonavanie lokalnych extremov
        // top(log(m recognized units)) = 6
        setIntProperty("neural_topology", 20); // topologia strednej vrstvy

        setStrProperty("help_file_help", "/help/help.html");
        setStrProperty("help_file_about", "/help/about.html");
        setStrProperty("reportgeneratorcss", "/reportgenerator/style.css");

        InputStream is = getResourceAsStream(fileName);
        if (is != null) {
            loadConfiguration(is);
            is.close();
        }
        Configurator.configurator = this;
    }

    public static synchronized Configurator getConfigurator() {
        if (configurator == null) {
            try {
                configurator = new Configurator();
            } catch (IOException e) {
                throw new IllegalStateException("Configurator failed to initialize.");
            }
        }
        return configurator;
    }

    public String getConfigurationFileName() {
        return fileName;
    }

    public void setConfigurationFileName(String name) {
        fileName = name;
    }

    public String getStrProperty(String name) {
        return list.getProperty(name);
    }

    public String getPathProperty(String name) {
        return getStrProperty(name).replace('/', File.separatorChar);

    }

    public void setStrProperty(String name, String value) {
        list.setProperty(name, value);
    }

    public int getIntProperty(String name) throws NumberFormatException {
        return Integer.decode(list.getProperty(name));
    }

    public void setIntProperty(String name, int value) {
        list.setProperty(name, String.valueOf(value));
    }

    public double getDoubleProperty(String name) throws NumberFormatException {
        return Double.parseDouble(list.getProperty(name));
    }

    public void setDoubleProperty(String name, double value) {
        list.setProperty(name, String.valueOf(value));
    }

    public Color getColorProperty(String name) {
        return new Color(Integer.decode(list.getProperty(name)));
    }

    public void setColorProperty(String name, Color value) {
        list.setProperty(name, String.valueOf(value.getRGB()));
    }

    public void saveConfiguration() throws IOException {
        saveConfiguration(fileName);
    }

    public void saveConfiguration(String arg_file) throws IOException {
        FileOutputStream os = new FileOutputStream(arg_file);
        list.storeToXML(os, null);
        os.close();
    }

    public void loadConfiguration() throws IOException {
        loadConfiguration(fileName);
    }

    public void loadConfiguration(String arg_file) throws IOException {
        InputStream is = getResourceAsStream(arg_file);
        loadConfiguration(is);
        if (is != null) {
            is.close();
        }
    }

    public void loadConfiguration(InputStream arg_stream) throws IOException {
        if (arg_stream == null) {
            list = null;
            return;
        }
        list.loadFromXML(arg_stream);
    }

    public InputStream getResourceAsStream(String filename) {
        String corrected = filename;
        URL f = getClass().getResource(corrected);
        if (f != null) {
            return getClass().getResourceAsStream(corrected);
        }

        if (filename.startsWith("/")) {
            corrected = filename.substring(1);
        } else if (filename.startsWith("./")) {
            corrected = filename.substring(2);
        } else {
            corrected = "/" + filename;
        }

        f = getClass().getResource(corrected);
        if (f != null) {
            return getClass().getResourceAsStream(corrected);
        }

        // Should actually load filename. It is here for the GUI. Loading images exactly from specified filesystem path
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
}
