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

package javaanpr.neuralnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("unused") //have a lot of them and need those variables
public class NeuralNetwork {
	// holds list of layers
	private Vector<NeuralLayer> listLayers = new Vector<NeuralLayer>();
	private Random randomGenerator;

	// rozmery su v poradi od najspodnejsej (input) po najvrchnejsiu (output)
	// vrstvu
	public NeuralNetwork(Vector<Integer> dimensions) {
		// initialization of layers
		for (int i = 0; i < dimensions.size(); i++) {
			listLayers.add(new NeuralLayer(dimensions.elementAt(i).intValue(),
					this));
		}
		randomGenerator = new Random();
		// System.out.println("Created neural network with "+dimensions.size()+" layers");
	}

	public NeuralNetwork(String fileName) throws ParserConfigurationException,
			SAXException, IOException, ParseException {
		loadFromXml(fileName);
		randomGenerator = new Random();
	}

	public Vector<Double> test(Vector<Double> inputs) {
		if (inputs.size() != getLayer(0).numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException(
					"[Error] NN-Test: You are trying to pass vector with "
							+ inputs.size()
							+ " values into neural layer with "
							+ getLayer(0).numberOfNeurons()
							+ " neurons. Consider using another network, or another descriptors.");
		} else {
			return activities(inputs);
		}
	}

	public void learn(SetOfIOPairs trainingSet, int maxK, double eps,
			double lambda, double micro) {
		if (trainingSet.pairs.size() == 0) {
			throw new NullPointerException(
					"[Error] NN-Learn: You are using an empty training set, neural network couldn't be trained.");
		} else if (trainingSet.pairs.elementAt(0).inputs.size() != getLayer(0)
				.numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException(
					"[Error] NN-Test: You are trying to pass vector with "
							+ trainingSet.pairs.elementAt(0).inputs.size()
							+ " values into neural layer with "
							+ getLayer(0).numberOfNeurons()
							+ " neurons. Consider using another network, or another descriptors.");
		} else if (trainingSet.pairs.elementAt(0).outputs.size() != getLayer(
				numberOfLayers() - 1).numberOfNeurons()) {
			throw new ArrayIndexOutOfBoundsException(
					"[Error] NN-Test:  You are trying to pass vector with "
							+ trainingSet.pairs.elementAt(0).inputs.size()
							+ " values into neural layer with "
							+ getLayer(0).numberOfNeurons()
							+ " neurons. Consider using another network, or another descriptors.");
		} else {
			adaptation(trainingSet, maxK, eps, lambda, micro);
		}
	}

	public int numberOfLayers() {
		return listLayers.size();
	}

	private void loadFromXml(String fileName)
			throws ParserConfigurationException, SAXException, IOException,
			ParseException {
		System.out
				.println("NeuralNetwork : loading network topology from file "
						+ fileName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(fileName);

		Node nodeNeuralNetwork = doc.getDocumentElement();
		if (!nodeNeuralNetwork.getNodeName().equals("neuralNetwork")) {
			throw new ParseException(
					"[Error] NN-Load: Parse error in XML file, neural network couldn't be loaded.",
					0);
		}
		// nodeNeuralNetwork ok
		// indexNeuralNetworkContent -> indexStructureContent ->
		// indexLayerContent -> indexNeuronContent -> indexNeuralInputContent
		NodeList nodeNeuralNetworkContent = nodeNeuralNetwork.getChildNodes();
		for (int innc = 0; innc < nodeNeuralNetworkContent.getLength(); innc++) {
			Node nodeStructure = nodeNeuralNetworkContent.item(innc);
			if (nodeStructure.getNodeName().equals("structure")) { // for
																	// structure
																	// element
				NodeList nodeStructureContent = nodeStructure.getChildNodes();
				for (int isc = 0; isc < nodeStructureContent.getLength(); isc++) {
					Node nodeLayer = nodeStructureContent.item(isc);
					if (nodeLayer.getNodeName().equals("layer")) { // for layer
																	// element
						NeuralLayer neuralLayer = new NeuralLayer(this);
						listLayers.add(neuralLayer);
						NodeList nodeLayerContent = nodeLayer.getChildNodes();
						for (int ilc = 0; ilc < nodeLayerContent.getLength(); ilc++) {
							Node nodeNeuron = nodeLayerContent.item(ilc);
							if (nodeNeuron.getNodeName().equals("neuron")) { // for
																				// neuron
																				// in
																				// layer
								Neuron neuron = new Neuron(
										Double.parseDouble(((Element) nodeNeuron)
												.getAttribute("threshold")),
										neuralLayer);
								neuralLayer.listNeurons.add(neuron);
								NodeList nodeNeuronContent = nodeNeuron
										.getChildNodes();
								for (int inc = 0; inc < nodeNeuronContent
										.getLength(); inc++) {
									Node nodeNeuralInput = nodeNeuronContent
											.item(inc);
									// if (nodeNeuralInput==null)
									// System.out.print("-"); else
									// System.out.print("*");

									if (nodeNeuralInput.getNodeName().equals(
											"input")) {
										// System.out.println("neuron at STR:"+innc+" LAY:"+isc+" NEU:"+ilc+" INP:"+inc);
										NeuralInput neuralInput = new NeuralInput(
												Double.parseDouble(((Element) nodeNeuralInput)
														.getAttribute("weight")),
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

	public void saveToXml(String fileName) throws ParserConfigurationException,
			FileNotFoundException, TransformerException,
			TransformerConfigurationException {
		System.out.println("Saving network topology to file " + fileName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.newDocument();

		Element root = doc.createElement("neuralNetwork");
		root.setAttribute("dateOfExport", new Date().toString());
		Element layers = doc.createElement("structure");
		layers.setAttribute("numberOfLayers",
				Integer.toString(numberOfLayers()));

		for (int il = 0; il < numberOfLayers(); il++) {
			Element layer = doc.createElement("layer");
			layer.setAttribute("index", Integer.toString(il));
			layer.setAttribute("numberOfNeurons",
					Integer.toString(getLayer(il).numberOfNeurons()));

			for (int in = 0; in < getLayer(il).numberOfNeurons(); in++) {
				Element neuron = doc.createElement("neuron");
				neuron.setAttribute("index", Integer.toString(in));
				neuron.setAttribute("NumberOfInputs", Integer
						.toString(getLayer(il).getNeuron(in).numberOfInputs()));
				neuron.setAttribute("threshold",
						Double.toString(getLayer(il).getNeuron(in).threshold));

				for (int ii = 0; ii < getLayer(il).getNeuron(in)
						.numberOfInputs(); ii++) {
					Element input = doc.createElement("input");
					input.setAttribute("index", Integer.toString(ii));
					input.setAttribute("weight", Double.toString(getLayer(il)
							.getNeuron(in).getInput(ii).weight));

					neuron.appendChild(input);
				}

				layer.appendChild(neuron);
			}

			layers.appendChild(layer);
		}

		root.appendChild(layers);
		doc.appendChild(root);

		// save
		File xmlOutputFile = new File(fileName);
		FileOutputStream fos;
		Transformer transformer;

		fos = new FileOutputStream(xmlOutputFile);
		// Use a Transformer for output
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fos);
		// transform source into result will do save
		transformer.setOutputProperty("encoding", "iso-8859-2");
		transformer.setOutputProperty("indent", "yes");
		transformer.transform(source, result);
	}

	public static class SetOfIOPairs {
		Vector<IOPair> pairs;

		public static class IOPair { // TU SOM PRIDAL STATIC, posovdne do tam
										// nebolo
			Vector<Double> inputs;
			Vector<Double> outputs;

			public IOPair(Vector<Double> inputs, Vector<Double> outputs) {
				// corrected warning
				// this.inputs = (Vector<Double>)inputs.clone();
				// this.outputs = (Vector<Double>)outputs.clone();
				this.inputs = new Vector<Double>(inputs);
				this.outputs = new Vector<Double>(outputs);
			}
		}

		public SetOfIOPairs() {
			pairs = new Vector<IOPair>();
		}

		public void addIOPair(Vector<Double> inputs, Vector<Double> outputs) {
			this.addIOPair(new IOPair(inputs, outputs));
		}

		public void addIOPair(IOPair pair) {
			pairs.add(pair);
		}

		int size() {
			return pairs.size();
		}
	}

	private class NeuralInput {
		double weight;
		int index;
		Neuron neuron;

		NeuralInput(double weight, Neuron neuron) {
			this.neuron = neuron;
			this.weight = weight;
			index = this.neuron.numberOfInputs();
			// System.out.println("Created neural input "+this.index+" with weight "+this.weight);
		}
	} // end class NeuralInput

	private class Neuron {
		private Vector<NeuralInput> listInputs = new Vector<NeuralInput>();// holds
																			// list
																			// of
																			// inputs
		int index;
		public double threshold;
		public double output;
		NeuralLayer neuralLayer;

		// initializes all neuron weights to 1 parameter specifies number of
		// weights

		Neuron(double threshold, NeuralLayer neuralLayer) {
			this.threshold = threshold;
			this.neuralLayer = neuralLayer;
			index = this.neuralLayer.numberOfNeurons();
		}

		Neuron(int numberOfInputs, double threshold, NeuralLayer neuralLayer) {
			this.threshold = threshold;
			this.neuralLayer = neuralLayer;
			index = this.neuralLayer.numberOfNeurons();
			for (int i = 0; i < numberOfInputs; i++) {
				listInputs.add(new NeuralInput(1.0, this));
			}
		}

		public int numberOfInputs() {
			return listInputs.size();
		}

		public NeuralInput getInput(int index) {
			return listInputs.elementAt(index);
		}

	} // end class Neuron

	private class NeuralLayer {
		// holds list od neurons
		private Vector<Neuron> listNeurons = new Vector<Neuron>();
		int index;
		NeuralNetwork neuralNetwork;

		NeuralLayer(NeuralNetwork neuralNetwork) {
			this.neuralNetwork = neuralNetwork;
			index = this.neuralNetwork.numberOfLayers();
		}

		// initializes all neurons in layer
		NeuralLayer(int numberOfNeurons, NeuralNetwork neuralNetwork) {
			this.neuralNetwork = neuralNetwork;
			index = this.neuralNetwork.numberOfLayers();
			// ak sa jedna o najnizsiu vrstvu (0), kazdy neuron bude mat iba 1
			// vstup
			for (int i = 0; i < numberOfNeurons; i++) {
				if (index == 0) {
					listNeurons.add(new Neuron(1, 0.0, this));
					/*
					 * prahy neuronov najnizsej vrstvy su vzdy 0.0, vrstva iba
					 * distribuuje vstupy, aspon tak to vyplyva z algoritmu na
					 * strane 111
					 */
				} else { // v opacnom pripade bude mat neuron tolko vstupov
							// kolko je neuronov nizsej vrstvy
					listNeurons.add(
					/*
					 * prahy neuronou na vyssich vrstvach budu tiez 0.0, ale
					 * nemusia byt
					 */
					new Neuron(this.neuralNetwork.getLayer(index - 1)
							.numberOfNeurons(), 0.0, this));
				}
			}
			// System.out.println("Created neural layer "+this.index+" with "+numberOfNeurons+" neurons");
		} // end constructor

		public int numberOfNeurons() {
			return listNeurons.size();
		}

		public boolean isLayerTop() {
			return (index == (neuralNetwork.numberOfLayers() - 1));
		}

		public boolean isLayerBottom() {
			return (index == 0);
		}

		public NeuralLayer upperLayer() {
			if (isLayerTop()) {
				return null;
			}
			return neuralNetwork.getLayer(index + 1);
		}

		public NeuralLayer lowerLayer() {
			if (isLayerBottom()) {
				return null;
			}
			return neuralNetwork.getLayer(index - 1);
		}

		public Neuron getNeuron(int index) {
			return listNeurons.elementAt(index);
		}

	} // end class NeuralLayer

	private class Gradients {
		Vector<Vector<Double>> thresholds;
		Vector<Vector<Vector<Double>>> weights;
		NeuralNetwork neuralNetwork;

		Gradients(NeuralNetwork network) {
			neuralNetwork = network;
			initGradients();
		}

		public void initGradients() {
			thresholds = new Vector<Vector<Double>>();
			weights = new Vector<Vector<Vector<Double>>>();
			// System.out.println("init for threshold gradient "+this.toString());
			for (int il = 0; il < neuralNetwork.numberOfLayers(); il++) {
				thresholds.add(new Vector<Double>());
				weights.add(new Vector<Vector<Double>>());
				for (int in = 0; in < neuralNetwork.getLayer(il)
						.numberOfNeurons(); in++) {
					thresholds.elementAt(il).add(0.0);
					weights.elementAt(il).add(new Vector<Double>());
					for (int ii = 0; ii < neuralNetwork.getLayer(il)
							.getNeuron(in).numberOfInputs(); ii++) {
						weights.elementAt(il).elementAt(in).add(0.0);
					} // for each input
				} // for each neuron
			} // for each layer
		}

		public void resetGradients() { // resets to 0
			for (int il = 0; il < neuralNetwork.numberOfLayers(); il++) {
				for (int in = 0; in < neuralNetwork.getLayer(il)
						.numberOfNeurons(); in++) {
					setThreshold(il, in, 0.0);
					for (int ii = 0; ii < neuralNetwork.getLayer(il)
							.getNeuron(in).numberOfInputs(); ii++) {
						setWeight(il, in, ii, 0.0);
					}
				}
			}
		}

		public double getThreshold(int il, int in) {
			return thresholds.elementAt(il).elementAt(in).doubleValue();
		}

		public void setThreshold(int il, int in, double value) {
			thresholds.elementAt(il).setElementAt(value, in);
		}

		public void incrementThreshold(int il, int in, double value) {
			setThreshold(il, in, getThreshold(il, in) + value);
		}

		public double getWeight(int il, int in, int ii) {
			return weights.elementAt(il).elementAt(in).elementAt(ii)
					.doubleValue();
		}

		public void setWeight(int il, int in, int ii, double value) {
			weights.elementAt(il).elementAt(in).setElementAt(value, ii);
		}

		public void incrementWeight(int il, int in, int ii, double value) {
			setWeight(il, in, ii, getWeight(il, in, ii) + value);
		}

		public double getGradientAbs() {
			double currE = 0;

			for (int il = 1; il < neuralNetwork.numberOfLayers(); il++) {
				currE += vectorAbs(thresholds.elementAt(il));
				currE += doubleVectorAbs(weights.elementAt(il));
			}
			return currE;

			// for (Vector<Double> vector : this.thresholds) currE +=
			// this.vectorAbs(vector);
			// for (Vector<Vector<Double>> doubleVector : this.weights) currE +=
			// this.doubleVectorAbs(doubleVector);
			// return currE;
		}

		private double doubleVectorAbs(Vector<Vector<Double>> doubleVector) {
			double totalX = 0;
			for (Vector<Double> vector : doubleVector) {
				totalX += Math.pow(vectorAbs(vector), 2);
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

	private double random() {
		return randomGenerator.nextDouble();
	}

	private void computeGradient(Gradients gradients, Vector<Double> inputs,
			Vector<Double> requiredOutputs) {
		// Gradients gradients = new Gradients(this);
		activities(inputs);
		for (int il = numberOfLayers() - 1; il >= 1; il--) { // backpropagation
																// cez
																// vsetky
																// vrstvy
																// okrem
																// poslednej
			NeuralLayer currentLayer = getLayer(il);

			if (currentLayer.isLayerTop()) { // ak sa jedna o najvyssiu vrstvu
				// pridame gradient prahov pre danu vrstvu do odpovedajuceho
				// vektora a tento gradient pocitame cez neurony :
				// gradients.thresholds.add(il, new Vector<Double>());
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // pre
																				// vsetky
																				// neurony
																				// na
																				// vrstve
					Neuron currentNeuron = currentLayer.getNeuron(in);
					gradients.setThreshold(
							il,
							in,
							currentNeuron.output
									* (1 - currentNeuron.output)
									* (currentNeuron.output - requiredOutputs
											.elementAt(in)));
				} // end for each neuron

				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // for
																				// each
																				// neuron
					Neuron currentNeuron = currentLayer.getNeuron(in);
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) { // for
																					// each
																					// neuron's
																					// input
						NeuralInput currentInput = currentNeuron.getInput(ii);
						gradients.setWeight(
								il,
								in,
								ii,
								gradients.getThreshold(il, in)
										* currentLayer.lowerLayer().getNeuron(
												ii).output);
					} // end for each input
				} // end for each neuron

			} else { // ak sa jedna o spodnejsie vrstvy (najnizsiu vrstvu
						// nepocitame, ideme len po 1.)
				// pocitame gradient prahov :
				// gradients.thresholds.add(il, new Vector<Double>());
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // for
																				// each
																				// neuron
					double aux = 0;
					// iterujeme cez vsetky axony neuronu (resp. synapsie
					// neuronov na vyssej vrstve)
					for (int ia = 0; ia < currentLayer.upperLayer()
							.numberOfNeurons(); ia++) {
						aux += gradients.getThreshold(il + 1, ia)
								* currentLayer.upperLayer().getNeuron(ia)
										.getInput(in).weight;
					}
					gradients.setThreshold(il, in,
							currentLayer.getNeuron(in).output
									* (1 - currentLayer.getNeuron(in).output)
									* aux);
				} // end for each neuron

				// pocitame gradienty vah :
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // for
																				// each
																				// neuron
					Neuron currentNeuron = currentLayer.getNeuron(in);
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) { // for
																					// each
																					// neuron's
																					// input
						NeuralInput currentInput = currentNeuron.getInput(ii);
						gradients.setWeight(
								il,
								in,
								ii,
								gradients.getThreshold(il, in)
										* currentLayer.lowerLayer().getNeuron(
												ii).output);
					} // end for each input
				} // end for each neuron

			} // end layer IF

		} // end backgropagation for each layer
			// return gradients;
	}

	private void computeTotalGradient(Gradients totalGradients,
			Gradients partialGradients, SetOfIOPairs trainingSet) {
		// na zaciatku sa inicializuju gradienty (total)
		totalGradients.resetGradients();
		// partialGradients.resetGradients();
		// Gradients totalGradients = new Gradients(this);
		// Gradients partialGradients = new Gradients(this); /***/

		for (SetOfIOPairs.IOPair pair : trainingSet.pairs) { // pre kazdy par
																// trenovacej
																// mnoziny
			// partialGradients = computeGradient(pair.inputs, pair.outputs);
			computeGradient(partialGradients, pair.inputs, pair.outputs);
			for (int il = numberOfLayers() - 1; il >= 1; il--) { // pre
																	// vsetky
																	// vrstvy
																	// okrem
																	// poslednej
				NeuralLayer currentLayer = getLayer(il);
				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // pre
																				// vsetky
																				// neurony
																				// na
																				// currentLayer
					// upravime gradient prahov :
					totalGradients.incrementThreshold(il, in,
							partialGradients.getThreshold(il, in));
					for (int ii = 0; ii < currentLayer.lowerLayer()
							.numberOfNeurons(); ii++) { // pre vsetky vstupy
						totalGradients.incrementWeight(il, in, ii,
								partialGradients.getWeight(il, in, ii));
					}
				}

			} // end for layer
		} // end foreach
			// return totalGradients;
	} // end method

	private void adaptation(SetOfIOPairs trainingSet, int maxK, double eps,
			double lambda, double micro) {
		// trainingSet : trenovacia mnozina
		// maxK : maximalny pocet iteracii
		// eps : pozadovana presnost normovanej dlzky gradientu
		// lambda : rychlost ucenia (0.1)
		// micro : momentovy clen
		double delta;
		Gradients deltaGradients = new Gradients(this);
		Gradients totalGradients = new Gradients(this);
		Gradients partialGradients = new Gradients(this);

		System.out.println("setting up random weights and thresholds ...");

		// prahy a vahy neuronovej siete nastavime na nahodne hodnoty,
		// delta-gradienty vynulujeme (oni sa nuluju uz pri init)
		for (int il = numberOfLayers() - 1; il >= 1; il--) { // iteracia
																// cez
																// vsetky
																// vrstvy
																// nadol
																// okrem
																// poslednej
			NeuralLayer currentLayer = getLayer(il);
			for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // pre
																			// kazdy
																			// neuron
																			// na
																			// vrstve
				Neuron currentNeuron = currentLayer.getNeuron(in);
				currentNeuron.threshold = (2 * random()) - 1;
				// deltaGradients.setThreshold(il,in,0.0);
				for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
					currentNeuron.getInput(ii).weight = (2 * random()) - 1;
					// deltaGradients.setWeight(il,in,ii,0.0);
				} // end ii
			} // end in
		} // end il

		int currK = 0; // citac iteracii
		double currE = Double.POSITIVE_INFINITY; // pociatocna aktualna presnost
													// bude nekonecna (tendencia
													// znizovania)

		System.out
				.println("entering adaptation loop ... (maxK = " + maxK + ")");

		while ((currK < maxK) && (currE > eps)) {
			computeTotalGradient(totalGradients, partialGradients, trainingSet);
			for (int il = numberOfLayers() - 1; il >= 1; il--) { // iteracia
																	// cez
																	// vsetky
																	// vrstvy
																	// nadol
																	// okrem
																	// poslednej
				NeuralLayer currentLayer = getLayer(il);

				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // pre
																				// kazdy
																				// neuron
																				// na
																				// vrstve
					Neuron currentNeuron = currentLayer.getNeuron(in);
					delta = (-lambda * totalGradients.getThreshold(il, in))
							+ (micro * deltaGradients.getThreshold(il, in));
					currentNeuron.threshold += delta;
					deltaGradients.setThreshold(il, in, delta);
				} // end for ii 1

				for (int in = 0; in < currentLayer.numberOfNeurons(); in++) { // pre
																				// kazdy
																				// neuron
																				// na
																				// vrstve
					Neuron currentNeuron = currentLayer.getNeuron(in);
					for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) { // a
																					// pre
																					// kazdy
																					// vstup
																					// neuronu
						delta = (-lambda * totalGradients.getWeight(il, in, ii))
								+ (micro * deltaGradients.getWeight(il, in, ii));
						currentNeuron.getInput(ii).weight += delta;
						deltaGradients.setWeight(il, in, ii, delta);
					} // end for ii
				} // end for in 2
			} // end for il

			currE = totalGradients.getGradientAbs();
			currK++;
			if ((currK % 25) == 0) {
				System.out.println("currK=" + currK + "   currE=" + currE);
			}
		} // end while
	}

	private Vector<Double> activities(Vector<Double> inputs) {
		for (int il = 0; il < numberOfLayers(); il++) { // pre kazdu vrstvu
			for (int in = 0; in < getLayer(il).numberOfNeurons(); in++) { // pre
																			// kazdy
																			// neuron
																			// vo
																			// vrstve
				double sum = getLayer(il).getNeuron(in).threshold; // sum
																	// <-
																	// threshold
				for (int ii = 0; ii < getLayer(il).getNeuron(in)
						.numberOfInputs(); ii++) { // vstupy
					// vynasobi vahu so vstupom
					if (il == 0) { // ak sme na najspodnejsej vrstve, nasobime
									// vahy so vstupmi
						sum += getLayer(il).getNeuron(in).getInput(ii).weight
								* inputs.elementAt(in).doubleValue();
					} else { // na hornych vrstvach nasobime vahy s vystupmi
								// nizsej vrstvy
						sum += getLayer(il).getNeuron(in).getInput(ii).weight
								* getLayer(il - 1).getNeuron(ii).output;
					}
				}

				// !!! TU SOM ROZLISIL CI SA JEDNA O PRVU VRSTVU :
				// if (il == 0)
				// this.getLayer(il).getNeuron(in).output = sum; // vystup
				// neuronu
				// else
				getLayer(il).getNeuron(in).output = gainFunction(sum);

				// this.getLayer(il).getNeuron(in).output =
				// this.gainFunction(sum); // vystup neuronu
			}
		}
		// nazaver vystupy neuronov najvyssej vrstvy zapiseme do vektora :
		Vector<Double> output = new Vector<Double>();

		for (int i = 0; i < getLayer(numberOfLayers() - 1).numberOfNeurons(); i++) {
			output.add(getLayer(numberOfLayers() - 1).getNeuron(i).output);
		}

		return output;
	}

	private double gainFunction(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	private NeuralLayer getLayer(int index) {
		return listLayers.elementAt(index);
	}

	/*
	 * public void printNeuralNetwork() { for (int il=0;
	 * il<this.numberOfLayers();il++) { System.out.println("Layer "+il); for
	 * (int in=0; in<this.getLayer(il).numberOfNeurons();in++) {
	 * System.out.print("      Neuron "+in+
	 * " (threshold="+this.getLayer(il).getNeuron(in).threshold+") : "); for
	 * (int ii=0; ii<this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
	 * System
	 * .out.print(this.getLayer(il).getNeuron(in).getInput(ii).weight+" "); }
	 * System.out.println(); } } }
	 */

} // end class NeuralNetwork
