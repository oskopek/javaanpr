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

package net.sf.javaanpr.recognizer;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.neuralnetwork.NeuralNetwork;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

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
