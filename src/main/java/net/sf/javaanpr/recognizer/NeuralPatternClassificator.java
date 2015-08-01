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

package net.sf.javaanpr.recognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.neuralnetwork.NeuralNetwork;

public class NeuralPatternClassificator extends CharacterRecognizer {

    private static int normalize_x = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_x");
    private static int normalize_y = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_y");

    // rozmer vstupneho pismena po transformacii : 10 x 16 = 160 neuronov

    public NeuralNetwork network;

    // do not learn netwotk, but load if from file (default)
    public NeuralPatternClassificator() {
        this(false);
    }

    public NeuralPatternClassificator(boolean learn) {
        Configurator configurator = Configurator.getConfigurator();

        // zakomentovane dna 2.1.2007
        // this.normalize_x =
        // configurator.getIntProperty("char_normalizeddimensions_x");
        // this.normalize_y =
        // configurator.getIntProperty("char_normalizeddimensions_y");

        Vector<Integer> dimensions = new Vector<Integer>();

        // determine size of input layer according to chosen feature extraction
        // method.
        int inputLayerSize;
        if (configurator.getIntProperty("char_featuresExtractionMethod") == 0) {
            inputLayerSize = NeuralPatternClassificator.normalize_x * NeuralPatternClassificator.normalize_y;
        } else {
            inputLayerSize = CharacterRecognizer.features.length * 4;
        }

        // construct new neural network with specified dimensions.
        dimensions.add(inputLayerSize);
        dimensions.add(configurator.getIntProperty("neural_topology"));
        dimensions.add(CharacterRecognizer.alphabet.length);
        this.network = new NeuralNetwork(dimensions);

        if (learn) {
            // learn network
            String learnAlphabetPath = configurator.getStrProperty("char_learnAlphabetPath");
            try {
                this.learnAlphabet(learnAlphabetPath);
            } catch (IOException e) {
                System.err.println("Failed to load alphabet: " + learnAlphabetPath);
                e.printStackTrace();
            }
        } else {
            // or load network from xml
            String neuralNetPath = configurator.getPathProperty("char_neuralNetworkPath");

            InputStream is = configurator.getResourceAsStream(neuralNetPath);

            this.network = new NeuralNetwork(is);
        }
    }

    // IMAGE -> CHAR
    @Override
    public RecognizedChar recognize(Char imgChar) { // rozpozna UZ normalizovany
        // char
        imgChar.normalize();
        Vector<Double> output = this.network.test(imgChar.extractFeatures());
        // double max = 0.0;
        // int indexMax = 0;

        RecognizedChar recognized = new RecognizedChar();

        for (int i = 0; i < output.size(); i++) {
            recognized.addPattern(recognized.new RecognizedPattern(alphabet[i], output.elementAt(i).floatValue()));
        }
        recognized.render();
        recognized.sort(1);
        return recognized;
    }

    // public Vector<Double> imageToVector(Char imgChar) {
    // Vector<Double> vectorInput = new Vector<Double>();
    // for (int x = 0; x<imgChar.getWidth(); x++)
    // for (int y = 0; y<imgChar.getHeight(); y++)
    // vectorInput.add(new Double(imgChar.getBrightness(x,y)));
    // return vectorInput;
    // }
    public NeuralNetwork.SetOfIOPairs.IOPair createNewPair(char chr, Char imgChar) { // uz normalizonvany
        Vector<Double> vectorInput = imgChar.extractFeatures();

        Vector<Double> vectorOutput = new Vector<Double>();
        for (int i = 0; i < alphabet.length; i++) {
            if (chr == alphabet[i]) {
                vectorOutput.add(1.0);
            } else {
                vectorOutput.add(0.0);
            }
        }


        System.out.println();
        for (Double d : vectorInput) {
            System.out.print(d + " ");
        }
        System.out.println();
        for (Double d : vectorOutput) {
            System.out.print(d + " ");
        }
        System.out.println();


        return (new NeuralNetwork.SetOfIOPairs.IOPair(vectorInput, vectorOutput));
    }

    // NAUCI NEURONOVU SIET ABECEDE, KTORU NAJDE V ADRESARI PATH
    public void learnAlphabet(String folder) throws IOException {
        NeuralNetwork.SetOfIOPairs train = new NeuralNetwork.SetOfIOPairs();

        ArrayList<String> fileList = (ArrayList<String>) Char.getAlphabetList(folder);

        for (String fileName : fileList) {
            InputStream is = Configurator.getConfigurator().getResourceAsStream(fileName);

            Char imgChar = new Char(is);
            imgChar.normalize();
            train.addIOPair(this.createNewPair(fileName.toUpperCase().charAt(0), imgChar));

            is.close();
        }

        this.network.learn(train, Configurator.getConfigurator().getIntProperty("neural_maxk"),
                Configurator.getConfigurator().getDoubleProperty("neural_eps"),
                Configurator.getConfigurator().getDoubleProperty("neural_lambda"),
                Configurator.getConfigurator().getDoubleProperty("neural_micro"));
    }
}
