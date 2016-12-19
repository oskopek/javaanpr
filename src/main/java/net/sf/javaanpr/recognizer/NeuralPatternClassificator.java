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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

public class NeuralPatternClassificator extends CharacterRecognizer {

    private static final transient Logger logger = LoggerFactory.getLogger(NeuralPatternClassificator.class);
    private static int normalize_x = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_x");
    private static int normalize_y = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_y");
    /**
     * Dimensions of an input character after transformation: 10 * 16 = 160 neurons.
     */
    private NeuralNetwork network;

    /**
     * Do not learn network, but load it from file (default).
     */
    public NeuralPatternClassificator() {
        this(false);
    }

    public NeuralPatternClassificator(boolean learn) {
        Configurator configurator = Configurator.getConfigurator();
        Vector<Integer> dimensions = new Vector<Integer>();
        // determine size of input layer according to chosen feature extraction method
        int inputLayerSize;
        if (configurator.getIntProperty("char_featuresExtractionMethod") == 0) {
            inputLayerSize = NeuralPatternClassificator.normalize_x * NeuralPatternClassificator.normalize_y;
        } else {
            inputLayerSize = CharacterRecognizer.FEATURES.length * 4;
        }
        // construct new neural network with specified dimensions
        dimensions.add(inputLayerSize);
        dimensions.add(configurator.getIntProperty("neural_topology"));
        dimensions.add(CharacterRecognizer.ALPHABET.length);
        this.network = new NeuralNetwork(dimensions);
        if (learn) {
            String learnAlphabetPath = configurator.getStrProperty("char_learnAlphabetPath");
            try {
                this.learnAlphabet(learnAlphabetPath);
            } catch (IOException e) {
                logger.error("Failed to load alphabet: {}", learnAlphabetPath);
            }
        } else {
            // or load network from xml
            String neuralNetPath = configurator.getPathProperty("char_neuralNetworkPath");
            InputStream is = configurator.getResourceAsStream(neuralNetPath);
            this.network = new NeuralNetwork(is);
        }
    }

    public NeuralNetwork getNetwork() {
        return network;
    }

    /**
     * Image to character.
     *
     * @param imgChar the Char to recognize
     * @return the {@link net.sf.javaanpr.recognizer.CharacterRecognizer.RecognizedChar}
     */
    @Override
    public RecognizedChar recognize(Char imgChar) {
        imgChar.normalize();
        Vector<Double> output = this.network.test(imgChar.extractFeatures());
        RecognizedChar recognized = new RecognizedChar();
        for (int i = 0; i < output.size(); i++) {
            recognized.addPattern(new RecognizedChar.RecognizedPattern(ALPHABET[i], output.elementAt(i).floatValue()));
        }
        recognized.render();
        recognized.sort(1);
        return recognized;
    }

    /**
     * @param chr the char
     * @param imgChar already normalized!
     * @return an {@link net.sf.javaanpr.neuralnetwork.NeuralNetwork.SetOfIOPairs.IOPair}
     */
    public NeuralNetwork.SetOfIOPairs.IOPair createNewPair(char chr, Char imgChar) {
        Vector<Double> vectorInput = imgChar.extractFeatures();
        Vector<Double> vectorOutput = new Vector<Double>();
        for (int i = 0; i < ALPHABET.length; i++) {
            if (chr == ALPHABET[i]) {
                vectorOutput.add(1.0);
            } else {
                vectorOutput.add(0.0);
            }
        }
        return (new NeuralNetwork.SetOfIOPairs.IOPair(vectorInput, vectorOutput));
    }

    /**
     * Learn the neural network with an alphabet in given folder.
     *
     * @param folder the alphabet folder
     * @throws IOException if the alphabet failed to load
     */
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
