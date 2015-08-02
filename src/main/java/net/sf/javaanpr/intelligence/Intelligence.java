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
import net.sf.javaanpr.gui.TimeMeter;
import net.sf.javaanpr.imageanalysis.Band;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.imageanalysis.HoughTransformation;
import net.sf.javaanpr.imageanalysis.Photo;
import net.sf.javaanpr.imageanalysis.Plate;
import net.sf.javaanpr.jar.Main;
import net.sf.javaanpr.recognizer.CharacterRecognizer;
import net.sf.javaanpr.recognizer.CharacterRecognizer.RecognizedChar;
import net.sf.javaanpr.recognizer.KnnPatternClassificator;
import net.sf.javaanpr.recognizer.NeuralPatternClassificator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

public class Intelligence {
    private static long lastProcessDuration = 0; // trvanie posledneho procesu v ms
    private static Configurator configurator = Configurator.getConfigurator();

    public static CharacterRecognizer chrRecog;
    public static Parser parser;

    public Intelligence() throws ParserConfigurationException, SAXException, IOException {

        int classification_method = configurator.getIntProperty("intelligence_classification_method");

        if (classification_method == 0) {
            chrRecog = new KnnPatternClassificator();
        } else {
            chrRecog = new NeuralPatternClassificator();
        }

        parser = new Parser();
    }

    /**
     * @return last process duration in milliseconds
     */
    public long lastProcessDuration() {
        return lastProcessDuration;
    }

    public String recognizeWithReport(CarSnapshot carSnapshot) throws IllegalArgumentException, IOException {
        final boolean enableReportGeneration = true;

        TimeMeter time = new TimeMeter();
        int syntaxAnalysisMode = configurator.getIntProperty("intelligence_syntaxanalysis");
        int skewDetectionMode = configurator.getIntProperty("intelligence_skewdetection");

        if (enableReportGeneration) {
            Main.rg.insertText("<h1>Automatic Number Plate Recognition Report</h1>");
            Main.rg.insertText("<span>Image width: " + carSnapshot.getWidth() + " px</span>");
            Main.rg.insertText("<span>Image height: " + carSnapshot.getHeight() + " px</span>");

            Main.rg.insertText("<h2>Vertical and Horizontal plate projection</h2>");

            Main.rg.insertImage(carSnapshot.renderGraph(), "snapshotgraph", 0, 0);
            Main.rg.insertImage(carSnapshot.getBiWithAxes(), "snapshot", 0, 0);
        }

        for (Band b : carSnapshot.getBands()) { // doporucene 3

            if (enableReportGeneration) {
                Main.rg.insertText("<div class='bandtxt'><h4>Band<br></h4>");
                Main.rg.insertImage(b.getBi(), "bandsmall", 250, 30);
                Main.rg.insertText("<span>Band width : " + b.getWidth() + " px</span>");
                Main.rg.insertText("<span>Band height : " + b.getHeight() + " px</span>");
                Main.rg.insertText("</div>");
            }

            for (Plate plate : b.getPlates()) { // doporucene 3

                if (enableReportGeneration) {
                    Main.rg.insertText("<div class='platetxt'><h4>Plate<br></h4>");
                    Main.rg.insertImage(plate.getBi(), "platesmall", 120, 30);
                    Main.rg.insertText("<span>Plate width : " + plate.getWidth() + " px</span>");
                    Main.rg.insertText("<span>Plate height : " + plate.getHeight() + " px</span>");
                    Main.rg.insertText("</div>");
                }

                // SKEW-RELATED
                Plate notNormalizedCopy = null;
                BufferedImage renderedHoughTransform;
                HoughTransformation hough = null;

                /*
                 * detekcia sa robi bud: 1. kvoli report generatoru 2. kvoli korekcii
                 */
                if (enableReportGeneration) { // || (skewDetectionMode != 0)) {
                    try {
                        notNormalizedCopy = plate.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
                    hough = notNormalizedCopy.getHoughTransformation();
                    renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
                }
                if (skewDetectionMode != 0) { // korekcia sa robi iba ak je
                    // zapnuta
                    AffineTransform shearTransform = AffineTransform.getShearInstance(0, -(double) hough.dy / hough.dx);
                    BufferedImage core = Photo.createBlankBi(plate.getBi());
                    core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
                    plate = new Plate(core);
                }

                plate.normalize();

                float plateWHratio = (float) plate.getWidth() / (float) plate.getHeight();
                if ((plateWHratio < configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio"))
                        || (plateWHratio > configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
                    continue;
                }

                Vector<Char> chars = plate.getChars();

                // heuristicka analyza znacky z pohladu uniformity a poctu
                // pismen :
                // Recognizer.configurator.getIntProperty("intelligence_minimumChars")
                if ((chars.size() < configurator.getIntProperty("intelligence_minimumChars"))
                        || (chars.size() > configurator.getIntProperty("intelligence_maximumChars"))) {
                    continue;
                }

                if (plate.getCharsWidthDispersion(chars) > configurator
                        .getDoubleProperty("intelligence_maxCharWidthDispersion")) {
                    continue;
                }

                /* ZNACKA PRIJATA, ZACINA NORMALIZACIA A HEURISTIKA PISMEN */

                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Detected band</h2>");
                    Main.rg.insertImage(b.getBiWithAxes(), "band", 0, 0);
                    Main.rg.insertImage(b.renderGraph(), "bandgraph", 0, 0);
                    Main.rg.insertText("<h2>Detected plate</h2>");
                    Plate plateCopy = null;
                    try {
                        plateCopy = plate.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    plateCopy.linearResize(450, 90);
                    Main.rg.insertImage(plateCopy.getBiWithAxes(), "plate", 0, 0);
                    Main.rg.insertImage(plateCopy.renderGraph(), "plategraph", 0, 0);
                }

                // SKEW-RELATED
                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Skew detection</h2>");
                    // Main.rg.insertImage(notNormalizedCopy.getBi());
                    Main.rg.insertImage(notNormalizedCopy.getBi(), "skewimage", 0, 0);
                    Main.rg.insertImage(renderedHoughTransform, "skewtransform", 0, 0);
                    Main.rg.insertText("Detected skew angle : <b>" + hough.angle + "</b>");
                }

                RecognizedPlate recognizedPlate = new RecognizedPlate();

                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Character segmentation</h2>");
                    Main.rg.insertText("<div class='charsegment'>");
                    for (Char chr : chars) {
                        Main.rg.insertImage(Photo.linearResizeBi(chr.getBi(), 70, 100), "", 0, 0);
                    }
                    Main.rg.insertText("</div>");
                }

                for (Char chr : chars) {
                    chr.normalize();
                }

                float averageHeight = plate.getAveragePieceHeight(chars);
                float averageContrast = plate.getAveragePieceContrast(chars);
                float averageBrightness = plate.getAveragePieceBrightness(chars);
                float averageHue = plate.getAveragePieceHue(chars);
                float averageSaturation = plate.getAveragePieceSaturation(chars);

                for (Char chr : chars) {
                    // heuristicka analyza jednotlivych pismen
                    boolean ok = true;
                    String errorFlags = "";

                    // pri normalizovanom pisme musime uvazovat pomer
                    float widthHeightRatio = (chr.pieceWidth);
                    widthHeightRatio /= (chr.pieceHeight);

                    if ((widthHeightRatio < configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio"))
                            || (widthHeightRatio > configurator
                            .getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
                        errorFlags += "WHR ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }

                    if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate.getWidth() - 1)))
                            && (widthHeightRatio < 0.12)) {
                        errorFlags += "POS ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }

                    // float similarityCost = rc.getSimilarityCost();

                    float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
                    float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
                    float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
                    float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
                    float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;

                    if (brightnessCost > configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
                        errorFlags += "BRI ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
                    if (contrastCost > configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
                        errorFlags += "CON ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
                    if (hueCost > configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
                        errorFlags += "HUE ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
                    if (saturationCost > configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
                        errorFlags += "SAT ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
                    if (heightCost < -configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
                        errorFlags += "HEI ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }

                    float similarityCost = 0;
                    RecognizedChar rc = null;
                    if (ok) {
                        rc = chrRecog.recognize(chr);
                        similarityCost = rc.getPatterns().elementAt(0).getCost();

                        if (similarityCost
                                > configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
                            errorFlags += "NEU ";
                            ok = false;
                            if (!enableReportGeneration) {
                                continue;
                            }
                        }

                    }

                    if (ok) {
                        recognizedPlate.addChar(rc);
                    }

                    if (enableReportGeneration) {
                        Main.rg.insertText("<div class='heuristictable'>");
                        Main.rg.insertImage(Photo.linearResizeBi(chr.getBi(), chr.getWidth() * 2, chr.getHeight() * 2),
                                "skeleton", 0, 0);
                        Main.rg.insertText("<span class='name'>WHR</span><span class='value'>" + widthHeightRatio
                                + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>HEI</span><span class='value'>" + heightCost + "</span>");
                        Main.rg.insertText("<span class='name'>NEU</span><span class='value'>" + similarityCost
                                + "</span>");
                        Main.rg.insertText("<span class='name'>CON</span><span class='value'>" + contrastCost
                                + "</span>");
                        Main.rg.insertText("<span class='name'>BRI</span><span class='value'>" + brightnessCost
                                + "</span>");
                        Main.rg.insertText("<span class='name'>HUE</span><span class='value'>" + hueCost + "</span>");
                        Main.rg.insertText("<span class='name'>SAT</span><span class='value'>" + saturationCost
                                + "</span>");
                        Main.rg.insertText("</table>");
                        if (errorFlags.length() != 0) {
                            Main.rg.insertText("<span class='errflags'>" + errorFlags + "</span>");
                        }
                        Main.rg.insertText("</div>");
                    }
                } // end for each char

                // nasledujuci riadok zabezpeci spracovanie dalsieho kandidata
                // na znacku, v pripade ze charrecognizingu je prilis malo
                // rozpoznanych pismen
                if (recognizedPlate.chars.size() < configurator.getIntProperty("intelligence_minimumChars")) {
                    continue;
                }

                lastProcessDuration = time.getTime();
                String parsedOutput = Intelligence.parser.parse(recognizedPlate, syntaxAnalysisMode);

                if (enableReportGeneration) {
                    Main.rg.insertText("<span class='recognized'>");
                    Main.rg.insertText("Recognized plate : " + parsedOutput);
                    Main.rg.insertText("</span>");
                }

                return parsedOutput;

            } // end for each plate

        }

        lastProcessDuration = time.getTime();
        // return new String("not available yet ;-)");
        return null;
    }

    public String recognize(CarSnapshot carSnapshot) {
        TimeMeter time = new TimeMeter();
        int syntaxAnalysisMode = configurator.getIntProperty("intelligence_syntaxanalysis");
        int skewDetectionMode = configurator.getIntProperty("intelligence_skewdetection");

        for (Band b : carSnapshot.getBands()) { // doporucene 3

            for (Plate plate : b.getPlates()) { // doporucene 3

                // SKEW-RELATED
                Plate notNormalizedCopy = null;

                @SuppressWarnings("unused")
                BufferedImage renderedHoughTransform = null;
                HoughTransformation hough = null;
                if (skewDetectionMode != 0) { // detekcia
                    // sa
                    // robi
                    // but
                    // 1)
                    // koli
                    // report
                    // generatoru
                    // 2)
                    // koli
                    // korekcii
                    try {
                        notNormalizedCopy = plate.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getBi());
                    hough = notNormalizedCopy.getHoughTransformation();
                    renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
                }
                if (skewDetectionMode != 0) { // korekcia sa robi iba ak je
                    // zapnuta
                    AffineTransform shearTransform = AffineTransform.getShearInstance(0, -(double) hough.dy / hough.dx);
                    BufferedImage core = Photo.createBlankBi(plate.getBi());
                    core.createGraphics().drawRenderedImage(plate.getBi(), shearTransform);
                    plate = new Plate(core);
                }

                plate.normalize();

                float plateWHratio = (float) plate.getWidth() / (float) plate.getHeight();
                if ((plateWHratio < configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio"))
                        || (plateWHratio > configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
                    continue;
                }

                Vector<Char> chars = plate.getChars();

                // heuristicka analyza znacky z pohladu uniformity a poctu
                // pismen :
                // Recognizer.configurator.getIntProperty("intelligence_minimumChars")
                if ((chars.size() < configurator.getIntProperty("intelligence_minimumChars"))
                        || (chars.size() > configurator.getIntProperty("intelligence_maximumChars"))) {
                    continue;
                }

                if (plate.getCharsWidthDispersion(chars) > configurator
                        .getDoubleProperty("intelligence_maxCharWidthDispersion")) {
                    continue;
                }

                /* ZNACKA PRIJATA, ZACINA NORMALIZACIA A HEURISTIKA PISMEN */

                RecognizedPlate recognizedPlate = new RecognizedPlate();

                for (Char chr : chars) {
                    chr.normalize();
                }

                float averageHeight = plate.getAveragePieceHeight(chars);
                float averageContrast = plate.getAveragePieceContrast(chars);
                float averageBrightness = plate.getAveragePieceBrightness(chars);
                float averageHue = plate.getAveragePieceHue(chars);
                float averageSaturation = plate.getAveragePieceSaturation(chars);

                for (Char chr : chars) {
                    // heuristicka analyza jednotlivych pismen
                    boolean ok = true;

                    @SuppressWarnings("unused")
                    String errorFlags = "";

                    // pri normalizovanom pisme musime uvazovat pomer
                    float widthHeightRatio = (chr.pieceWidth);
                    widthHeightRatio /= (chr.pieceHeight);

                    if ((widthHeightRatio < configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio"))
                            || (widthHeightRatio > configurator
                            .getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
                        errorFlags += "WHR ";
                        ok = false;
                        continue;
                    }

                    if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate.getWidth() - 1)))
                            && (widthHeightRatio < 0.12)) {
                        errorFlags += "POS ";
                        ok = false;
                        continue;
                    }

                    // float similarityCost = rc.getSimilarityCost();

                    float contrastCost = Math.abs(chr.statisticContrast - averageContrast);
                    float brightnessCost = Math.abs(chr.statisticAverageBrightness - averageBrightness);
                    float hueCost = Math.abs(chr.statisticAverageHue - averageHue);
                    float saturationCost = Math.abs(chr.statisticAverageSaturation - averageSaturation);
                    float heightCost = (chr.pieceHeight - averageHeight) / averageHeight;

                    if (brightnessCost > configurator.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
                        errorFlags += "BRI ";
                        ok = false;
                        continue;
                    }
                    if (contrastCost > configurator.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
                        errorFlags += "CON ";
                        ok = false;
                        continue;
                    }
                    if (hueCost > configurator.getDoubleProperty("intelligence_maxHueCostDispersion")) {
                        errorFlags += "HUE ";
                        ok = false;
                        continue;
                    }
                    if (saturationCost > configurator.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
                        errorFlags += "SAT ";
                        ok = false;
                        continue;
                    }
                    if (heightCost < -configurator.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
                        errorFlags += "HEI ";
                        ok = false;
                        continue;
                    }

                    float similarityCost;
                    RecognizedChar rc = null;
                    if (ok) {
                        rc = chrRecog.recognize(chr);
                        similarityCost = rc.getPatterns().elementAt(0).getCost();

                        if (similarityCost
                                > configurator.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
                            errorFlags += "NEU ";
                            ok = false;
                            continue;
                        }

                    }

                    if (ok) {
                        recognizedPlate.addChar(rc);
                    }
                } // end for each char

                // nasledujuci riadok zabezpeci spracovanie dalsieho kandidata
                // na znacku, v pripade ze charrecognizingu je prilis malo
                // rozpoznanych pismen
                if (recognizedPlate.chars.size() < configurator.getIntProperty("intelligence_minimumChars")) {
                    continue;
                }

                lastProcessDuration = time.getTime();
                return Intelligence.parser.parse(recognizedPlate, syntaxAnalysisMode);

            } // end for each plate

        }

        lastProcessDuration = time.getTime();
        // return new String("not available yet ;-)");
        return null;
    }
}
