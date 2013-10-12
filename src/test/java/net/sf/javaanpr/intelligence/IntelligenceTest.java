/**
 * 
 */
package net.sf.javaanpr.intelligence;

import static org.junit.Assert.*;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Vector;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.gui.TimeMeter;
import net.sf.javaanpr.imageanalysis.Band;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.imageanalysis.HoughTransformation;
import net.sf.javaanpr.imageanalysis.Photo;
import net.sf.javaanpr.imageanalysis.Plate;
import net.sf.javaanpr.recognizer.CharacterRecognizer;
import net.sf.javaanpr.recognizer.CharacterRecognizer.RecognizedChar.RecognizedPattern;
import net.sf.javaanpr.recognizer.KnnPatternClassificator;
import net.sf.javaanpr.recognizer.NeuralPatternClassificator;
import net.sf.javaanpr.recognizer.CharacterRecognizer.RecognizedChar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author oskopek
 *
 */
public class IntelligenceTest {
	
	private static long lastProcessDuration = 0; // trvanie posledneho procesu v ms
	private static Configurator configurator = Configurator.getConfigurator();
	
	public static CharacterRecognizer chrRecog;
	public static Parser parser;
	
	private CarSnapshot carSnapshot;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		int classification_method = configurator
				.getIntProperty("intelligence_classification_method");
		System.out.println(classification_method);

		if (classification_method == 0) {
			chrRecog = new KnnPatternClassificator();
		} else {
			chrRecog = new NeuralPatternClassificator();
		}
		
		assertNotNull(chrRecog);

		parser = new Parser();
		assertNotNull(parser);
		
		this.carSnapshot = new CarSnapshot("snapshots/test_006.jpg");
		assertNotNull(carSnapshot);
		assertNotNull(carSnapshot.image);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		carSnapshot.close();
		carSnapshot = null;
		chrRecog = null;
		parser = null;
	}

	/**
	 * Test method for {@link net.sf.javaanpr.intelligence.Intelligence#recognizeWithReport(net.sf.javaanpr.imageanalysis.CarSnapshot)}.
	 */
	@Test
	@Ignore
	public void testRecognizeWithReport() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link net.sf.javaanpr.intelligence.Intelligence#recognize(net.sf.javaanpr.imageanalysis.CarSnapshot)}.
	 * @throws Exception 
	 */
	@Test
	public void testRecognize() throws Exception {
		TimeMeter time = new TimeMeter();
		int syntaxAnalysisMode = configurator
				.getIntProperty("intelligence_syntaxanalysis");
		int skewDetectionMode = configurator
				.getIntProperty("intelligence_skewdetection");
		
		for (Band b : carSnapshot.getBands()) { // doporucene 3
			
			for (Plate plate : b.getPlates()) {// doporucene 3

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
					notNormalizedCopy = plate.clone();
					notNormalizedCopy.horizontalEdgeDetector(notNormalizedCopy
							.getBi());
					hough = notNormalizedCopy.getHoughTransformation();
					renderedHoughTransform = hough.render(
							HoughTransformation.RENDER_ALL,
							HoughTransformation.COLOR_BW);
				}
				if (skewDetectionMode != 0) { // korekcia sa robi iba ak je
												// zapnuta
					AffineTransform shearTransform = AffineTransform
							.getShearInstance(0, -(double) hough.dy / hough.dx);
					BufferedImage core = Photo.createBlankBi(plate.getBi());
					core.createGraphics().drawRenderedImage(plate.getBi(),
							shearTransform);
					plate = new Plate(core);
				}

				plate.normalize();

				float plateWHratio = (float) plate.getWidth()
						/ (float) plate.getHeight();
				if ((plateWHratio < configurator
						.getDoubleProperty("intelligence_minPlateWidthHeightRatio"))
						|| (plateWHratio > configurator
								.getDoubleProperty("intelligence_maxPlateWidthHeightRatio"))) {
					continue;
				}

				Vector<Char> chars = plate.getChars();

				// heuristicka analyza znacky z pohladu uniformity a poctu
				// pismen :
				// Recognizer.configurator.getIntProperty("intelligence_minimumChars")
				if ((chars.size() < configurator
						.getIntProperty("intelligence_minimumChars"))
						|| (chars.size() > configurator
								.getIntProperty("intelligence_maximumChars"))) {
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
				float averageBrightness = plate
						.getAveragePieceBrightness(chars);
				float averageHue = plate.getAveragePieceHue(chars);
				float averageSaturation = plate
						.getAveragePieceSaturation(chars);

				for (Char chr : chars) {
					// heuristicka analyza jednotlivych pismen
					boolean ok = true;
					
					@SuppressWarnings("unused")
					String errorFlags = "";

					// pri normalizovanom pisme musime uvazovat pomer
					float widthHeightRatio = (chr.pieceWidth);
					widthHeightRatio /= (chr.pieceHeight);

					if ((widthHeightRatio < configurator
							.getDoubleProperty("intelligence_minCharWidthHeightRatio"))
							|| (widthHeightRatio > configurator
									.getDoubleProperty("intelligence_maxCharWidthHeightRatio"))) {
						errorFlags += "WHR ";
						ok = false;
						continue;
					}

					if (((chr.positionInPlate.x1 < 2) || (chr.positionInPlate.x2 > (plate
							.getWidth() - 1))) && (widthHeightRatio < 0.12)) {
						errorFlags += "POS ";
						ok = false;
						continue;
					}

					// float similarityCost = rc.getSimilarityCost();

					float contrastCost = Math.abs(chr.statisticContrast
							- averageContrast);
					float brightnessCost = Math
							.abs(chr.statisticAverageBrightness
									- averageBrightness);
					float hueCost = Math.abs(chr.statisticAverageHue
							- averageHue);
					float saturationCost = Math
							.abs(chr.statisticAverageSaturation
									- averageSaturation);
					float heightCost = (chr.pieceHeight - averageHeight)
							/ averageHeight;

					if (brightnessCost > configurator
							.getDoubleProperty("intelligence_maxBrightnessCostDispersion")) {
						errorFlags += "BRI ";
						ok = false;
						continue;
					}
					if (contrastCost > configurator
							.getDoubleProperty("intelligence_maxContrastCostDispersion")) {
						errorFlags += "CON ";
						ok = false;
						continue;
					}
					if (hueCost > configurator
							.getDoubleProperty("intelligence_maxHueCostDispersion")) {
						errorFlags += "HUE ";
						ok = false;
						continue;
					}
					if (saturationCost > configurator
							.getDoubleProperty("intelligence_maxSaturationCostDispersion")) {
						errorFlags += "SAT ";
						ok = false;
						continue;
					}
					if (heightCost < -configurator
							.getDoubleProperty("intelligence_maxHeightCostDispersion")) {
						errorFlags += "HEI ";
						ok = false;
						continue;
					}

					float similarityCost = 0;
					RecognizedChar rc = null;
					assertNull(rc);
					if (ok == true) {
						rc = chrRecog.recognize(chr);
						assertNotNull(rc);
						similarityCost = rc.getPatterns().elementAt(0).getCost();
						
						if (similarityCost > configurator
								.getDoubleProperty("intelligence_maxSimilarityCostDispersion")) {
							errorFlags += "NEU ";
							ok = false;
							continue;
						}

					}

					if (ok == true) {
						for(RecognizedPattern c : rc.getPatterns()) {
							System.out.print(c.getChar());
							recognizedPlate.addChar(rc);
						}

					    System.out.println("");
					}
				} // end for each char

				// nasledujuci riadok zabezpeci spracovanie dalsieho kandidata
				// na znacku, v pripade ze charrecognizingu je prilis malo
				// rozpoznanych pismen
				if (recognizedPlate.chars.size() < configurator
						.getIntProperty("intelligence_minimumChars")) {
					continue;
				}
				

				lastProcessDuration = time.getTime();
				String parsedOutput = parser.parse(
						recognizedPlate, syntaxAnalysisMode);
				
				System.out.println(parsedOutput);
				return;

			} // end for each plate

		}

		lastProcessDuration = time.getTime();
		// return new String("not available yet ;-)");
		System.out.println("null string");
		return;
	}

}
