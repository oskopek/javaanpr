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
import net.sf.javaanpr.imageanalysis.*;
import net.sf.javaanpr.intelligence.parser.Parser;
import net.sf.javaanpr.jar.Main;
import net.sf.javaanpr.recognizer.CharacterRecognizer;
import net.sf.javaanpr.recognizer.RecognizedChar;
import net.sf.javaanpr.recognizer.KnnPatternClassifier;
import net.sf.javaanpr.recognizer.NeuralPatternClassifier;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class Intelligence {

    private static CharacterRecognizer chrRecog;
    private static Parser parser;
    private static long lastProcessDuration = 0L;

    private static final Configurator configurator = Configurator.getConfigurator();

    public Intelligence() throws ParserConfigurationException, SAXException, IOException {
        int classification_method = configurator.getIntProperty("intelligence_classification_method");
        if (classification_method == 0) {
            chrRecog = new KnnPatternClassifier();
        } else {
            chrRecog = new NeuralPatternClassifier();
        }
        parser = new Parser();
    }

    /**
     * @return last process duration in milliseconds
     */
    public long getLastProcessDuration() {
        return lastProcessDuration;
    }

    public String recognize(CarSnapshot carSnapshot) throws IllegalArgumentException, IOException  {
        return recognize(carSnapshot, false);
    }

    // TODO refactor with forms
    public String recognize(CarSnapshot carSnapshot, final boolean enableReportGeneration)
            throws IllegalArgumentException, IOException {
        TimeMeter time = new TimeMeter();
        int syntaxAnalysisModeInt = configurator.getIntProperty("intelligence_syntaxanalysis");
        SyntaxAnalysisMode syntaxAnalysisMode = SyntaxAnalysisMode.getSyntaxAnalysisModeFromInt(syntaxAnalysisModeInt);
        int skewDetectionMode = configurator.getIntProperty("intelligence_skewdetection");
        if (enableReportGeneration) {
            Main.rg.insertText("<h1>Automatic Number Plate Recognition Report</h1>");
            Main.rg.insertText("<span>Image width: " + carSnapshot.getWidth() + " px</span>");
            Main.rg.insertText("<span>Image height: " + carSnapshot.getHeight() + " px</span>");
            Main.rg.insertText("<h2>Vertical and Horizontal plate projection</h2>");
            Main.rg.insertImage(carSnapshot.renderGraph(), "snapshotgraph", 0, 0);
            Main.rg.insertImage(carSnapshot.getBiWithAxes(), "snapshot", 0, 0);
        }
        for (Band b : carSnapshot.getBands()) {
            if (enableReportGeneration) {
                Main.rg.insertText("<div class='bandtxt'><h4>Band<br></h4>");
                Main.rg.insertImage(b.getImage(), "bandsmall", 250, 30);
                Main.rg.insertText("<span>Band width : " + b.getWidth() + " px</span>");
                Main.rg.insertText("<span>Band height : " + b.getHeight() + " px</span>");
                Main.rg.insertText("</div>");
            }
            for (Plate plate : b.getPlates()) {
                if (enableReportGeneration) {
                    Main.rg.insertText("<div class='platetxt'><h4>Plate<br></h4>");
                    Main.rg.insertImage(plate.getImage(), "platesmall", 120, 30);
                    Main.rg.insertText("<span>Plate width : " + plate.getWidth() + " px</span>");
                    Main.rg.insertText("<span>Plate height : " + plate.getHeight() + " px</span>");
                    Main.rg.insertText("</div>");
                }
                // Skew-related
                Plate notNormalizedCopy = null;
                BufferedImage renderedHoughTransform = null;
                HoughTransformation hough = null;
                // detection is done either: 1. because of the report generator 2. because of skew detection
                if (enableReportGeneration || skewDetectionMode != 0) {
                    notNormalizedCopy = plate.clone();
                    notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy.getImage());
                    hough = notNormalizedCopy.getHoughTransformation();
                    renderedHoughTransform = hough.render(HoughTransformation.RENDER_ALL, HoughTransformation.COLOR_BW);
                }
                if (skewDetectionMode != 0) { // skew detection on
                    AffineTransform shearTransform =
                            AffineTransform.getShearInstance(0, -(double) hough.getDy() / hough.getDx());
                    BufferedImage core = Photo.createBlankBi(plate.getImage());
                    core.createGraphics().drawRenderedImage(plate.getImage(), shearTransform);
                    plate = new Plate(core);
                }
                plate.normalize();

                float plateWHratio = (float) plate.getWidth() / (float) plate.getHeight();
                if ((plateWHratio < configurator.getDoubleProperty("intelligence_minPlateWidthHeightRatio")) || (
                        plateWHratio > configurator.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
                    continue;
                }
                List<Char> chars = plate.getChars();

                // heuristic analysis of the plate (uniformity and character count)
                if ((chars.size() < configurator.getIntProperty("intelligence_minimumChars")) || (chars.size()
                        > configurator.getIntProperty("intelligence_maximumChars"))) {
                    continue;
                }
                if (plate.getCharsWidthDispersion(chars) > configurator
                        .getDoubleProperty("intelligence_maxCharWidthDispersion")) {
                    continue;
                }

                // Plate accepted; normalize and begin character heuristic
                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Detected band</h2>");
                    Main.rg.insertImage(b.getBiWithAxes(), "band", 0, 0);
                    Main.rg.insertImage(b.renderGraph(), "bandgraph", 0, 0);
                    Main.rg.insertText("<h2>Detected plate</h2>");
                    Plate plateCopy = plate.clone();
                    plateCopy.linearResize(450, 90);
                    Main.rg.insertImage(plateCopy.getBiWithAxes(), "plate", 0, 0);
                    Main.rg.insertImage(plateCopy.renderGraph(), "plategraph", 0, 0);
                }
                // Skew-related
                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Skew detection</h2>");
                    Main.rg.insertImage(notNormalizedCopy.getImage(), "skewimage", 0, 0);
                    Main.rg.insertImage(renderedHoughTransform, "skewtransform", 0, 0);
                    Main.rg.insertText("Detected skew angle : <b>" + hough.getAngle() + "</b>");
                }
                RecognizedPlate recognizedPlate = new RecognizedPlate();
                if (enableReportGeneration) {
                    Main.rg.insertText("<h2>Character segmentation</h2>");
                    Main.rg.insertText("<div class='charsegment'>");
                    for (Char chr : chars) {
                        Main.rg.insertImage(Photo.linearResizeBi(chr.getImage(), 70, 100), "", 0, 0);
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
                    // heuristic analysis of individual characters
                    boolean ok = true;
                    String errorFlags = "";
                    // when normalizing the chars, keep the width/height ratio in mind
                    float widthHeightRatio = (chr.pieceWidth);
                    widthHeightRatio /= (chr.pieceHeight);
                    if ((widthHeightRatio < configurator.getDoubleProperty("intelligence_minCharWidthHeightRatio")) || (
                            widthHeightRatio > configurator
                                    .getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
                        errorFlags += "WHR ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
                    if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate.getWidth() - 1))) && (
                            widthHeightRatio < 0.12)) {
                        errorFlags += "POS ";
                        ok = false;
                        if (!enableReportGeneration) {
                            continue;
                        }
                    }
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
                        similarityCost = rc.getPatterns().get(0).getCost();
                        if (similarityCost > configurator
                                .getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
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
                        Main.rg.insertImage(
                                Photo.linearResizeBi(chr.getImage(), chr.getWidth() * 2, chr.getHeight() * 2),
                                "skeleton", 0, 0);
                        Main.rg.insertText(
                                "<span class='name'>WHR</span><span class='value'>" + widthHeightRatio + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>HEI</span><span class='value'>" + heightCost + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>NEU</span><span class='value'>" + similarityCost + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>CON</span><span class='value'>" + contrastCost + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>BRI</span><span class='value'>" + brightnessCost + "</span>");
                        Main.rg.insertText("<span class='name'>HUE</span><span class='value'>" + hueCost + "</span>");
                        Main.rg.insertText(
                                "<span class='name'>SAT</span><span class='value'>" + saturationCost + "</span>");
                        Main.rg.insertText("</table>");
                        if (errorFlags.length() != 0) {
                            Main.rg.insertText("<span class='errflags'>" + errorFlags + "</span>");
                        }
                        Main.rg.insertText("</div>");
                    }
                }
                // if too few characters recognized, get next candidate
                if (recognizedPlate.getChars().size() < configurator.getIntProperty("intelligence_minimumChars")) {
                    continue;
                }
                lastProcessDuration = time.getTime();
                String parsedOutput = Intelligence.parser.parse(recognizedPlate, syntaxAnalysisMode);
                if (enableReportGeneration) {
                    Main.rg.insertText("<span class='recognized'>");
                    Main.rg.insertText("Recognized plate : " + parsedOutput);
                    Main.rg.insertText("</span>");
                    Main.rg.finish();
                }
                return parsedOutput;
            }
        }
        // TODO failed!
        lastProcessDuration = time.getTime();
        if (enableReportGeneration) {
            Main.rg.finish();
        }
        return null;
    }
}
