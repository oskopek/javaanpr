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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

public class KnnPatternClassificator extends CharacterRecognizer {
    Vector<Vector<Double>> learnVectors;

    public KnnPatternClassificator() {
        String path = Configurator.getConfigurator().getPathProperty("char_learnAlphabetPath");

        // inicializacia vektora na pozadovanu velkost (nulovanie poloziek)
        this.learnVectors = new Vector<Vector<Double>>(36);

        ArrayList<String> filenames = (ArrayList<String>) Char.getAlphabetList(path);

        for (String fileName : filenames) {
            InputStream is = Configurator.getConfigurator().getResourceAsStream(fileName);

            Char imgChar = null;

            try {
                imgChar = new Char(is);

            } catch (IOException e) {
                System.err.println("Failed to load Char: " + fileName);
                e.printStackTrace();
            }
            imgChar.normalize();
            // zapis na danu poziciu vo vektore
            this.learnVectors.add(imgChar.extractFeatures());
        }

        // kontrola poloziek vektora
        for (int i = 0; i < this.learnVectors.size(); i++) {
            if (this.learnVectors.elementAt(i) == null) {
                System.err.println("Warning : alphabet in " + path + " is not complete");
            }
        }

    }

    @Override
    public RecognizedChar recognize(Char chr) {
        Vector<Double> tested = chr.extractFeatures();
        // int minx = 0;
        // float minfx = Float.POSITIVE_INFINITY;

        RecognizedChar recognized = new RecognizedChar();

        for (int x = 0; x < this.learnVectors.size(); x++) {
            // pre lepsie fungovanie bol pouhy rozdiel vektorov nahradeny
            // euklidovskou vzdialenostou
            float fx = this.simplifiedEuclideanDistance(tested, this.learnVectors.elementAt(x));

            recognized.addPattern(recognized.new RecognizedPattern(alphabet[x], fx));

            // if (fx < minfx) {
            // minfx = fx;
            // minx = x;
            // }
        }
        // return new RecognizedChar(this.alphabet[minx], minfx);
        recognized.sort(0);
        return recognized;
    }

    @SuppressWarnings("unused")
    // Maybe will be used eventually again
    private float difference(Vector<Double> vectorA, Vector<Double> vectorB) {
        float diff = 0;
        for (int x = 0; x < vectorA.size(); x++) {
            diff += Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
        }
        return diff;
    }

    private float simplifiedEuclideanDistance(Vector<Double> vectorA, Vector<Double> vectorB) {
        float diff = 0;
        float partialDiff;
        for (int x = 0; x < vectorA.size(); x++) {
            partialDiff = (float) Math.abs(vectorA.elementAt(x) - vectorB.elementAt(x));
            diff += partialDiff * partialDiff;
        }
        return diff;
    }

}
