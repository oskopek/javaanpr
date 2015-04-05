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

package net.sf.javaanpr.jar;

import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;

import net.sf.javaanpr.configurator.Configurator;
import net.sf.javaanpr.gui.ReportGenerator;
import net.sf.javaanpr.gui.windows.FrameComponentInit;
import net.sf.javaanpr.gui.windows.FrameMain;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.imageanalysis.Char;
import net.sf.javaanpr.intelligence.Intelligence;
import net.sf.javaanpr.recognizer.NeuralPatternClassificator;

/**
 * <p>Main class which initializes the project, taking input parameters from command line and then running the project
 * accordingly.</p>
 * <p/>
 * Allowed parameters are:
 * <ul>
 * <li>-help   Displays the help messsage.
 * <li>-gui   Run GUI viewer (default choice).
 * <li>-recognize -i "snapshot"   Recognize single snapshot.
 * <li>-recognize -i "snapshot" -o "dstdir"   Recognize single snapshot and save report html into specified directory.
 * <li>-newconfig -o "file"   Generate default configuration file.
 * <li>-newnetwork -o "file"   Train neural network according to specified feature extraction method and learning
 * parameters (in config. file) and saves it into output file.
 * <li>-newalphabet -i "srcdir" -o "dstdir"   Normalize all images in "srcdir" and save it to "dstdir".
 * </ul>
 *
 * @author Ondrej Martinsky.
 */
public class Main {

    /**
     * The report generator.
     */
    public static ReportGenerator rg = new ReportGenerator();

    /**
     * The intelligence.
     */
    public static Intelligence systemLogic;

    /**
     * The help message.
     */
    public static String helpText = ""
            + "-----------------------------------------------------------\n"
            + "Automatic number plate recognition system\n"
            + "Copyright (c) Ondrej Martinsky, 2006-2007\n"
            + "\n"
            + "Licensed under the Educational Community License,\n"
            + "\n" + "Usage : java -jar anpr.jar [-options]\n"
            + "\n" + "Where options include:\n"
            + "\n"
            + "    -help         Displays this help\n"
            + "    -gui          Run GUI viewer (default choice)\n"
            + "    -recognize -i <snapshot>\n"
            + "                  Recognize single snapshot\n"
            + "    -recognize -i <snapshot> -o <dstdir>\n"
            + "                  Recognize single snapshot and\n"
            + "                  save report html into specified\n"
            + "                  directory\n"
            + "    -newconfig -o <file>\n"
            + "                  Generate default configuration file\n"
            + "    -newnetwork -o <file>\n"
            + "                  Train neural network according to\n"
            + "                  specified feature extraction method and\n"
            + "                  learning parameters (in config. file)\n"
            + "                  and saves it into output file\n"
            + "    -newalphabet -i <srcdir> -o <dstdir>\n"
            + "                  Normalize all images in <srcdir> and save\n"
            + "                  it to <dstdir>.";

    /**
     * Normalizes the alphabet in the source directory and writes the result to the target directory.
     *
     * @param srcdir the source directory.
     * @param dstdir the destination directory.
     * @throws IOException
     */
    public static void newAlphabet(String srcdir, String dstdir) throws IOException { // NOT USED

        int x = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_x");
        int y = Configurator.getConfigurator().getIntProperty("char_normalizeddimensions_y");
        System.out.println("\nCreating new alphabet (" + x + " x " + y + " px)... \n");

        for (String fileName : Char.getAlphabetList(srcdir)) {
            Char c = new Char(fileName);
            c.normalize();
            c.saveImage(dstdir + File.separator + fileName);
            System.out.println(fileName + " done");
            c.close();
        }
    }

    /**
     * Train neural network according to specified feature extraction method and learning parameters (in config file)
     * and saves it into output file.
     *
     * @param destinationFile the destination file.
     * @throws Exception
     */
    public static void learnAlphabet(String destinationFile) throws Exception {
        try {
            File f = new File(destinationFile);
            f.createNewFile();
        } catch (Exception e) {
            throw new IOException("Can't find the path specified");
        }
        System.out.println();
        NeuralPatternClassificator npc = new NeuralPatternClassificator(true);
        npc.network.saveToXml(destinationFile);
    }

    /**
     * Main method which parses the input parameters and then runs the project accordingly.
     *
     * @param args the input parameters.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if ((args.length == 0) || ((args.length == 1) && args[0].equals("-gui"))) {
            // DONE run gui
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            FrameComponentInit frameComponentInit = new FrameComponentInit(); // show wait
            Main.systemLogic = new Intelligence();
            frameComponentInit.dispose(); // hide wait
            new FrameMain();
        } else if ((args.length == 3) && args[0].equals("-recognize") && args[1].equals("-i")) {
            // DONE load snapshot args[2] and recognize it
            try {
                Main.systemLogic = new Intelligence();
                System.out.println(Main.systemLogic.recognize(new CarSnapshot(args[2])));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if ((args.length == 5) && args[0].equals("-recognize") && args[1].equals("-i") && args[3].equals("-o")) {
            // load snapshot arg[2] and generate report into arg[4]
            try {
                Main.rg = new ReportGenerator(args[4]); // prepare report generator
                Main.systemLogic = new Intelligence(); // prepare intelligence
                Main.systemLogic.recognizeWithReport(new CarSnapshot(args[2]));
                Main.rg.finish();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        } else if ((args.length == 3) && args[0].equals("-newconfig") && args[1].equals("-o")) {
            // DONE save default config into args[2]
            try {
                Configurator.getConfigurator().saveConfiguration(args[2]);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if ((args.length == 3) && args[0].equals("-newnetwork") && args[1].equals("-o")) {
            // DONE learn new neural network and save it into into args[2]
            try {
                Main.learnAlphabet(args[2]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if ((args.length == 5) && args[0].equals("-newalphabet") && args[1].equals("-i") && args[3].equals("-o")) {
            // DONE transform alphabets from args[2] -> args[4]
            try {
                Main.newAlphabet(args[2], args[4]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            // DONE display help
            System.out.println(Main.helpText);
        }
    }
}
