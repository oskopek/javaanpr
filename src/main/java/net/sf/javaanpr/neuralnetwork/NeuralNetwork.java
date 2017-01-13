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

package net.sf.javaanpr.neuralnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

/**
 * Have a lot of them and need those variables.
 */
public class NeuralNetwork { // TODO: finish translation

    private static final transient Logger logger = LoggerFactory.getLogger(NeuralNetwork.class);
    /**
     * Holds a list of layers.
     */
    private final Vector<NeuralLayer> listLayers = new Vector<NeuralLayer>();
    private final Random randomGenerator;

    /**
     * Dimensions are in order from the lowest (input) to highest (output) layer.
     *
     * @param dimensions number of neurons in each layer
     */
    public NeuralNetwork(Vector<Integer> dimensions) {
        for (int i = 0; i < dimensions.size(); i++) {
            this.listLayers.add(new NeuralLayer(dimensions.elementAt(i), this));
        }
        this.randomGenerator = new Random();
        logger.info("Created neural network with " + dimensions.size() + " layers");
    }

    public NeuralNetwork(InputStream inStream) {
        this.loadFromXml(inStream);
        this.randomGenerator = new Random();
    }

    public Vector<Double> test(Vector<Double> inputs) {
        if (inputs.size() != this.getLayer(0).numberOfNeurons()) {
            throw new ArrayIndexOutOfBoundsException(
                    "[Error] NN-Test: You are trying to pass vector with " + inputs.size()
                            + " values into neural layer with " + this.getLayer(0).numberOfNeurons() + " neurons. "
                            + "Consider using another network, or another descriptors.");
        } else {
            return this.activities(inputs);
        }
    }

    public void learn(SetOfIOPairs trainingSet, int maxK, double eps, double lambda, double micro) {
        if (trainingSet.pairs.size() == 0) {
            throw new NullPointerException(
                    "[Error] NN-Learn: You are using an empty training set, neural network couldn't be trained.");
        } else if (trainingSet.pairs.elementAt(0).inputs.size() != this.getLayer(0).numberOfNeurons()) {
            throw new ArrayIndexOutOfBoundsException(
                    "[Error] NN-Test: You are trying to pass vector with " + trainingSet.pairs.elementAt(0).inputs
                            .size() + " values into neural layer with " + this.getLayer(0).numberOfNeurons()
                            + " neurons. Consider using another network, or another " + "descriptors.");
        } else if (trainingSet.pairs.elementAt(0).outputs.size() != this.getLayer(this.numberOfLayers() - 1)
                .numberOfNeurons()) {
            throw new ArrayIndexOutOfBoundsException(
                    "[Error] NN-Test:  You are trying to pass vector with " + trainingSet.pairs.elementAt(0).inputs
                            .size() + " values into neural layer with " + this.getLayer(0).numberOfNeurons()
                            + " neurons. Consider using another network, or another " + "descriptors.");
        } else {
            this.adaptation(trainingSet, maxK, eps, lambda, micro);
        }
    }

    public int numberOfLayers() {
        return this.listLayers.size();
    }

    private void loadFromXml(InputStream inStream) {
        logger.debug("Loading network topology from InputStream");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder parser = factory.newDocumentBuilder();
            doc = parser.parse(inStream);
            if (doc == null) {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Node nodeNeuralNetwork = doc.getDocumentElement();
        if (!nodeNeuralNetwork.getNodeName().equals("neuralNetwork")) {
            logger.error("Parse error in XML file, neural network couldn't be loaded.");
        }
        NodeList nodeNeuralNetworkContent = nodeNeuralNetwork.getChildNodes();
        for (int innc = 0; innc < nodeNeuralNetworkContent.getLength(); innc++) {
            Node nodeStructure = nodeNeuralNetworkContent.item(innc);
            if (nodeStructure.getNodeName().equals("structure")) {
                NodeList nodeStructureContent = nodeStructure.getChildNodes();
                for (int isc = 0; isc < nodeStructureContent.getLength(); isc++) {
                    Node nodeLayer = nodeStructureContent.item(isc);
                    if (nodeLayer.getNodeName().equals("layer")) {
                        NeuralLayer neuralLayer = new NeuralLayer(this);
                        this.listLayers.add(neuralLayer);
                        NodeList nodeLayerContent = nodeLayer.getChildNodes();
                        for (int ilc = 0; ilc < nodeLayerContent.getLength(); ilc++) {
                            Node nodeNeuron = nodeLayerContent.item(ilc);
                            if (nodeNeuron.getNodeName().equals("neuron")) {
                                Neuron neuron =
                                        new Neuron(Double.parseDouble(((Element) nodeNeuron).getAttribute("threshold")),
                                                neuralLayer);
                                neuralLayer.listNeurons.add(neuron);
                                NodeList nodeNeuronContent = nodeNeuron.getChildNodes();
                                for (int inc = 0; inc < nodeNeuronContent.getLength(); inc++) {
                                    Node nodeNeuralInput = nodeNeuronContent.item(inc);
                                    if (nodeNeuralInput.getNodeName().equals("input")) {
                                        logger.debug("neuron at STR: {} LAY: {} NEU: {} INP: {}", innc, isc, ilc, inc);
                                        NeuralInput neuralInput = new NeuralInput(
                                                Double.parseDouble(((Element) nodeNeuralInput).getAttribute("weight")),
                                                neuron);
                                        neuron.listInputs.add(neuralInput);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public void saveToXml(String fileName)
            throws ParserConfigurationException, FileNotFoundException, TransformerException {
        logger.info("Saving network topology to file " + fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.newDocument();

        Element root = doc.createElement("neuralNetwork");
        root.setAttribute("dateOfExport", new Date().toString());
        Element layers = doc.createElement("structure");
        layers.setAttribute("numberOfLayers", Integer.toString(this.numberOfLayers()));
        for (int il = 0; il < this.numberOfLayers(); il++) {
            Element layer = doc.createElement("layer");
            layer.setAttribute("index", Integer.toString(il));
            layer.setAttribute("numberOfNeurons", Integer.toString(this.getLayer(il).numberOfNeurons()));
            for (int in = 0; in < this.getLayer(il).numberOfNeurons(); in++) {
                Element neuron = doc.createElement("neuron");
                neuron.setAttribute("index", Integer.toString(in));
                neuron.setAttribute("NumberOfInputs",
                        Integer.toString(this.getLayer(il).getNeuron(in).numberOfInputs()));
                neuron.setAttribute("threshold", Double.toString(this.getLayer(il).getNeuron(in).threshold));
                for (int ii = 0; ii < this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
                    Element input = doc.createElement("input");
                    input.setAttribute("index", Integer.toString(ii));
                    input.setAttribute("weight", Double.toString(this.getLayer(il).getNeuron(in).getInput(ii).weight));
                    neuron.appendChild(input);
                }
                layer.appendChild(neuron);
            }
            layers.appendChild(layer);
        }
        root.appendChild(layers);
        doc.appendChild(root);

        File xmlOutputFile = new File(fileName);
        FileOutputStream fos = new FileOutputStream(xmlOutputFile);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fos);
        // transform source into result will do save
        transformer.setOutputProperty("encoding", "iso-8859-2");
        transformer.setOutputProperty("indent", "yes");
        transformer.transform(source, result);
    }

    private double random() {
        return this.randomGenerator.nextDouble();
    }

    private void computeGradient(Gradients gradients, Vector<Double> inputs, Vector<Double> requiredOutputs) {
        this.activities(inputs);
        for (int layerIndex = this.numberOfLayers() - 1; layerIndex >= 1;
                layerIndex--) { // backpropagation cez vsetky vrstvy okrem poslednej
            NeuralLayer currentLayer = this.getLayer(layerIndex);
            if (currentLayer.isLayerTop()) {
                // ak sa jedna o najvyssiu vrstvu
                // pridame gradient prahov pre danu vrstvu do odpovedajuceho
                // vektora a tento gradient pocitame cez neurony:
                // gradients.thresholds.add(layerIndex, new Vector<Double>());
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                    gradients.setThreshold(layerIndex, neuronIndex,
                            currentNeuron.output * (1 - currentNeuron.output) * (currentNeuron.output - requiredOutputs
                                    .elementAt(neuronIndex)));
                }
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                    for (int inputIndex = 0; inputIndex < currentNeuron.numberOfInputs(); inputIndex++) {
                        NeuralInput currentInput = currentNeuron.getInput(inputIndex);
                        gradients.setWeight(layerIndex, neuronIndex, inputIndex,
                                gradients.getThreshold(layerIndex, neuronIndex) * currentLayer.lowerLayer()
                                        .getNeuron(inputIndex).output);
                    }
                }
            } else {
                // ak sa jedna o spodnejsie vrstvy (najnizsiu vrstvu
                // nepocitame, ideme len po 1.)
                // pocitame gradient prahov :
                // gradients.thresholds.add(layerIndex, new Vector<Double>());
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    double aux = 0;
                    // iterujeme cez vsetky axony neuronu (resp. synapsie neuronov na vyssej vrstve)
                    for (int axonIndex = 0; axonIndex < currentLayer.upperLayer().numberOfNeurons(); axonIndex++) {
                        aux += gradients.getThreshold(layerIndex + 1, axonIndex) * currentLayer.upperLayer()
                                .getNeuron(axonIndex).getInput(neuronIndex).weight;
                    }
                    gradients.setThreshold(layerIndex, neuronIndex,
                            currentLayer.getNeuron(neuronIndex).output * (1 - currentLayer
                                    .getNeuron(neuronIndex).output) * aux);
                }
                // pocitame gradienty vah :
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                    for (int inputIndex = 0; inputIndex < currentNeuron.numberOfInputs(); inputIndex++) {
                        NeuralInput currentInput = currentNeuron.getInput(inputIndex);
                        gradients.setWeight(layerIndex, neuronIndex, inputIndex,
                                gradients.getThreshold(layerIndex, neuronIndex) * currentLayer.lowerLayer()
                                        .getNeuron(inputIndex).output);
                    }
                }
            }
        }
    }

    private void computeTotalGradient(Gradients totalGradients, Gradients partialGradients, SetOfIOPairs trainingSet) {
        totalGradients.resetGradients();
        for (SetOfIOPairs.IOPair pair : trainingSet.pairs) {
            this.computeGradient(partialGradients, pair.inputs, pair.outputs);
            for (int layerIndex = this.numberOfLayers() - 1; layerIndex >= 1;
                    layerIndex--) { // all layers except last one
                NeuralLayer currentLayer = this.getLayer(layerIndex);
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    // upravime gradient prahov :
                    totalGradients.incrementThreshold(layerIndex, neuronIndex,
                            partialGradients.getThreshold(layerIndex, neuronIndex));
                    for (int inputIndex = 0; inputIndex < currentLayer.lowerLayer().numberOfNeurons(); inputIndex++) {
                        totalGradients.incrementWeight(layerIndex, neuronIndex, inputIndex,
                                partialGradients.getWeight(layerIndex, neuronIndex, inputIndex));
                    }
                }
            }
        }
    }

    /**
     * @param trainingSet training set
     * @param maxK maximum number of iterations
     * @param eps epsilon, required accuracy of normal gradient length
     * @param lambda speed of learning (0.1)
     * @param micro moment factor
     */
    private void adaptation(SetOfIOPairs trainingSet, int maxK, double eps, double lambda, double micro) {
        double delta;
        Gradients deltaGradients = new Gradients(this);
        Gradients totalGradients = new Gradients(this);
        Gradients partialGradients = new Gradients(this);
        logger.debug("Setting up random weights and thresholds ...");
        // prahy a vahy neuronovej siete nastavime na nahodne hodnoty,
        // delta-gradienty vynulujeme (oni sa nuluju uz pri init)
        for (int layerIndex = this.numberOfLayers() - 1; layerIndex >= 1;
                layerIndex--) { // top down all layers except last one
            NeuralLayer currentLayer = this.getLayer(layerIndex);
            for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                currentNeuron.threshold = (2 * this.random()) - 1;
                for (int inputIndex = 0; inputIndex < currentNeuron.numberOfInputs(); inputIndex++) {
                    currentNeuron.getInput(inputIndex).weight = (2 * this.random()) - 1;
                }
            }
        }

        int curK = 0;
        double curE = Double.POSITIVE_INFINITY; // pociatocna aktualna presnost bude nekonecna (tendencia znizovania)
        logger.debug("Entering adaptation loop ... (maxK = " + maxK + ")");
        while ((curK < maxK) && (curE > eps)) {
            this.computeTotalGradient(totalGradients, partialGradients, trainingSet);
            for (int layerIndex = this.numberOfLayers() - 1; layerIndex >= 1;
                    layerIndex--) { // top down all layers except last one
                NeuralLayer currentLayer = this.getLayer(layerIndex);
                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                    delta = (-lambda * totalGradients.getThreshold(layerIndex, neuronIndex)) + (micro * deltaGradients
                            .getThreshold(layerIndex, neuronIndex));
                    currentNeuron.threshold += delta;
                    deltaGradients.setThreshold(layerIndex, neuronIndex, delta);
                }

                for (int neuronIndex = 0; neuronIndex < currentLayer.numberOfNeurons(); neuronIndex++) {
                    Neuron currentNeuron = currentLayer.getNeuron(neuronIndex);
                    for (int inputIndex = 0; inputIndex < currentNeuron.numberOfInputs(); inputIndex++) {
                        delta = (-lambda * totalGradients.getWeight(layerIndex, neuronIndex, inputIndex)) + (micro
                                * deltaGradients.getWeight(layerIndex, neuronIndex, inputIndex));
                        currentNeuron.getInput(inputIndex).weight += delta;
                        deltaGradients.setWeight(layerIndex, neuronIndex, inputIndex, delta);
                    }
                }
            }
            curE = totalGradients.getGradientAbs();
            curK++;
            if ((curK % 25) == 0) {
                logger.debug("curK=" + curK + ", curE=" + curE);
            }
        }
    }

    private Vector<Double> activities(Vector<Double> inputs) {
        for (int layerIndex = 0; layerIndex < this.numberOfLayers(); layerIndex++) {
            for (int neuronIndex = 0; neuronIndex < this.getLayer(layerIndex).numberOfNeurons(); neuronIndex++) {
                double sum = this.getLayer(layerIndex).getNeuron(neuronIndex).threshold; // sum <- threshold
                for (int inputIndex = 0; inputIndex < this.getLayer(layerIndex).getNeuron(neuronIndex).numberOfInputs();
                        inputIndex++) { // vstupy
                    // vynasobi vahu so vstupom
                    if (layerIndex == 0) { // ak sme na najspodnejsej vrstve, nasobime vahy so vstupmi
                        sum += this.getLayer(layerIndex).getNeuron(neuronIndex).getInput(inputIndex).weight * inputs
                                .elementAt(neuronIndex);
                    } else { // na hornych vrstvach nasobime vahy s vystupmi nizsej vrstvy
                        sum += this.getLayer(layerIndex).getNeuron(neuronIndex).getInput(inputIndex).weight * this
                                .getLayer(layerIndex - 1).getNeuron(inputIndex).output;
                    }
                }
                this.getLayer(layerIndex).getNeuron(neuronIndex).output = this.gainFunction(sum);
            }
        }
        Vector<Double> output = new Vector<Double>();
        for (int i = 0; i < this.getLayer(this.numberOfLayers() - 1).numberOfNeurons(); i++) {
            output.add(this.getLayer(this.numberOfLayers() - 1).getNeuron(i).output);
        }
        return output;
    }

    private double gainFunction(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    private NeuralLayer getLayer(int index) {
        return this.listLayers.elementAt(index);
    }

    public void printNeuralNetwork() {
        for (int layerIndex = 0; layerIndex < this.numberOfLayers(); layerIndex++) {
            System.out.println("Layer " + layerIndex);
            for (int neuronIndex = 0; neuronIndex < this.getLayer(layerIndex).numberOfNeurons(); neuronIndex++) {
                System.out.print("      Neuron " + neuronIndex + " (threshold=" + this.getLayer(layerIndex)
                        .getNeuron(neuronIndex).threshold + ") : ");
                for (int inputIndex = 0; inputIndex < this.getLayer(layerIndex).getNeuron(neuronIndex).numberOfInputs();
                        inputIndex++) {
                    System.out
                            .print(this.getLayer(layerIndex).getNeuron(neuronIndex).getInput(inputIndex).weight + " ");
                }
                System.out.println();
            }
        }
    }

    public static class SetOfIOPairs {

        private final Vector<IOPair> pairs;

        public SetOfIOPairs() {
            this.pairs = new Vector<IOPair>();
        }

        public void addIOPair(Vector<Double> inputs, Vector<Double> outputs) {
            this.addIOPair(new IOPair(inputs, outputs));
        }

        public void addIOPair(IOPair pair) {
            this.pairs.add(pair);
        }

        int size() {
            return this.pairs.size();
        }

        public static class IOPair {
            private final Vector<Double> inputs;
            private final Vector<Double> outputs;

            public IOPair(Vector<Double> inputs, Vector<Double> outputs) {
                this.inputs = new Vector<Double>(inputs);
                this.outputs = new Vector<Double>(outputs);
            }
        }
    }

    private final class NeuralInput {
        private double weight;
        private final int index;
        private final Neuron neuron;

        private NeuralInput(double weight, Neuron neuron) {
            this.neuron = neuron;
            this.weight = weight;
            this.index = this.neuron.numberOfInputs();
            logger.debug("Created neural input {} with weight {}.", this.index, this.weight);
        }
    }

    private final class Neuron {
        private double threshold;
        private double output;
        private final int index;
        private final NeuralLayer neuralLayer;
        private final Vector<NeuralInput> listInputs = new Vector<NeuralInput>();

        private Neuron(double threshold, NeuralLayer neuralLayer) {
            this.threshold = threshold;
            this.neuralLayer = neuralLayer;
            this.index = this.neuralLayer.numberOfNeurons();
        }

        /**
         * Initializes all neuron weights to 1.0d.
         *
         * @param numberOfInputs number of weights of the neuron
         * @param threshold the threshold of the neuron
         * @param neuralLayer which layer the neuron belongs to
         */
        private Neuron(int numberOfInputs, double threshold, NeuralLayer neuralLayer) {
            this.threshold = threshold;
            this.neuralLayer = neuralLayer;
            this.index = this.neuralLayer.numberOfNeurons();
            for (int i = 0; i < numberOfInputs; i++) {
                this.listInputs.add(new NeuralInput(1.0d, this));
            }
        }

        public int numberOfInputs() {
            return this.listInputs.size();
        }

        public NeuralInput getInput(int index) {
            return this.listInputs.elementAt(index);
        }

    }

    private final class NeuralLayer {
        private final int index;
        private final NeuralNetwork neuralNetwork;
        private final Vector<Neuron> listNeurons = new Vector<Neuron>();

        private NeuralLayer(NeuralNetwork neuralNetwork) {
            this.neuralNetwork = neuralNetwork;
            this.index = this.neuralNetwork.numberOfLayers();
        }

        /**
         * Initializes all neurons in the layer.
         *
         * @param numberOfNeurons the number of neurons in this layer
         * @param neuralNetwork the network
         */
        private NeuralLayer(int numberOfNeurons, NeuralNetwork neuralNetwork) {
            this.neuralNetwork = neuralNetwork;
            this.index = this.neuralNetwork.numberOfLayers();
            for (int i = 0; i < numberOfNeurons; i++) {
                if (this.index == 0) {
                    // on the lowest layer (0), each neuron has 1 input
                    this.listNeurons.add(new Neuron(1, 0.0, this));
                    // threshold of neurons on the lowest layer are always 0.0, the layer only distributes inputs
                    // (algorithm, page 111)
                } else {
                    // the thresholds of neurons on higher layers are also 0.0, but they don't have to be
                    this.listNeurons
                            .add(new Neuron(this.neuralNetwork.getLayer(this.index - 1).numberOfNeurons(), 0.0, this));
                }
            }
            logger.debug("Created neural layer {} with {} neurons.", this.index, numberOfNeurons);
        }

        public int numberOfNeurons() {
            return this.listNeurons.size();
        }

        public boolean isLayerTop() {
            return (this.index == (this.neuralNetwork.numberOfLayers() - 1));
        }

        public boolean isLayerBottom() {
            return (this.index == 0);
        }

        public NeuralLayer upperLayer() {
            if (this.isLayerTop()) {
                return null;
            }
            return this.neuralNetwork.getLayer(this.index + 1);
        }

        public NeuralLayer lowerLayer() {
            if (this.isLayerBottom()) {
                return null;
            }
            return this.neuralNetwork.getLayer(this.index - 1);
        }

        public Neuron getNeuron(int index) {
            return this.listNeurons.elementAt(index);
        }

    }

    private final class Gradients {
        private Vector<Vector<Double>> thresholds;
        private Vector<Vector<Vector<Double>>> weights;
        private final NeuralNetwork neuralNetwork;

        private Gradients(NeuralNetwork network) {
            this.neuralNetwork = network;
            this.initGradients();
        }

        private void initGradients() {
            this.thresholds = new Vector<Vector<Double>>();
            this.weights = new Vector<Vector<Vector<Double>>>();
            logger.debug("Init for threshold gradient: {} ", this);
            for (int layerIndex = 0; layerIndex < this.neuralNetwork.numberOfLayers(); layerIndex++) {
                this.thresholds.add(new Vector<Double>());
                this.weights.add(new Vector<Vector<Double>>());
                for (int neuronIndex = 0; neuronIndex < this.neuralNetwork.getLayer(layerIndex).numberOfNeurons();
                        neuronIndex++) {
                    this.thresholds.elementAt(layerIndex).add(0.0);
                    this.weights.elementAt(layerIndex).add(new Vector<Double>());
                    for (int inputIndex = 0; inputIndex < this.neuralNetwork.getLayer(layerIndex).getNeuron(neuronIndex)
                            .numberOfInputs(); inputIndex++) {
                        this.weights.elementAt(layerIndex).elementAt(neuronIndex).add(0.0);
                    }
                }
            }
        }

        /**
         * Resets gradients to 0.
         */
        public void resetGradients() {
            for (int layerIndex = 0; layerIndex < this.neuralNetwork.numberOfLayers(); layerIndex++) {
                for (int neuronIndex = 0; neuronIndex < this.neuralNetwork.getLayer(layerIndex).numberOfNeurons();
                        neuronIndex++) {
                    this.setThreshold(layerIndex, neuronIndex, 0.0d);
                    for (int inputIndex = 0; inputIndex < this.neuralNetwork.getLayer(layerIndex).getNeuron(neuronIndex)
                            .numberOfInputs(); inputIndex++) {
                        this.setWeight(layerIndex, neuronIndex, inputIndex, 0.0d);
                    }
                }
            }
        }

        public double getThreshold(int layerIndex, int neuronIndex) {
            return this.thresholds.elementAt(layerIndex).elementAt(neuronIndex);
        }

        public void setThreshold(int layerIndex, int neuronIndex, double value) {
            this.thresholds.elementAt(layerIndex).setElementAt(value, neuronIndex);
        }

        public void incrementThreshold(int layerIndex, int neuronIndex, double value) {
            this.setThreshold(layerIndex, neuronIndex, this.getThreshold(layerIndex, neuronIndex) + value);
        }

        public double getWeight(int layerIndex, int neuronIndex, int inputIndex) {
            return this.weights.elementAt(layerIndex).elementAt(neuronIndex).elementAt(inputIndex);
        }

        public void setWeight(int layerIndex, int neuronIndex, int inputIndex, double value) {
            this.weights.elementAt(layerIndex).elementAt(neuronIndex).setElementAt(value, inputIndex);
        }

        public void incrementWeight(int layerIndex, int neuronIndex, int inputIndex, double value) {
            this.setWeight(layerIndex, neuronIndex, inputIndex,
                    this.getWeight(layerIndex, neuronIndex, inputIndex) + value);
        }

        public double getGradientAbs() {
            double currE = 0;
            for (int layerIndex = 1; layerIndex < this.neuralNetwork.numberOfLayers(); layerIndex++) {
                currE += this.vectorAbs(this.thresholds.elementAt(layerIndex));
                currE += this.doubleVectorAbs(this.weights.elementAt(layerIndex));
            }
            return currE;
        }

        private double doubleVectorAbs(Vector<Vector<Double>> doubleVector) {
            double totalX = 0;
            for (Vector<Double> vector : doubleVector) {
                totalX += Math.pow(this.vectorAbs(vector), 2);
            }
            return Math.sqrt(totalX);
        }

        private double vectorAbs(Vector<Double> vector) {
            double totalX = 0;
            for (Double x : vector) {
                totalX += Math.pow(x, 2);
            }
            return Math.sqrt(totalX);
        }
    }
}
