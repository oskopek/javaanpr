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

package net.sf.javaanpr.imageanalysis;

import net.sf.javaanpr.configurator.Configurator;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class CarSnapshot extends Photo {
    private static int distributor_margins = Configurator.getConfigurator().getIntProperty(
            "carsnapshot_distributormargins");
    // private static int carsnapshot_projectionresize_x =
    // Main.configurator.getIntProperty("carsnapshot_projectionresize_x");
    // private static int carsnapshot_projectionresize_y =
    // Main.configurator.getIntProperty("carsnapshot_projectionresize_y");
    private static int carsnapshot_graphrankfilter = Configurator.getConfigurator().getIntProperty(
            "carsnapshot_graphrankfilter");

    private static int numberOfCandidates = Configurator.getConfigurator().getIntProperty("intelligence_numberOfBands");
    private CarSnapshotGraph graphHandle = null;

    public static Graph.ProbabilityDistributor distributor = new Graph.ProbabilityDistributor(0, 0,
            CarSnapshot.distributor_margins, CarSnapshot.distributor_margins);

    public CarSnapshot(String filename) throws IOException {
        super(Configurator.getConfigurator().getResourceAsStream(filename));

    }

    public CarSnapshot(BufferedImage bi) {
        super(bi);
    }

    public CarSnapshot(InputStream is) throws IOException {
        super(is);
    }

    public BufferedImage renderGraph() {
        this.computeGraph();
        return this.graphHandle.renderVertically(100, this.getHeight());
    }

    private Vector<Graph.Peak> computeGraph() {
        if (this.graphHandle != null) {
            return this.graphHandle.peaks; // graf uz bol vypocitany
        }

        BufferedImage imageCopy = duplicateBufferedImage(this.image);
        this.verticalEdgeBi(imageCopy);
        Photo.thresholding(imageCopy); // strasne moc zere

        this.graphHandle = this.histogram(imageCopy);
        this.graphHandle.rankFilter(CarSnapshot.carsnapshot_graphrankfilter);
        this.graphHandle.applyProbabilityDistributor(CarSnapshot.distributor);

        this.graphHandle.findPeaks(CarSnapshot.numberOfCandidates); // sort by height
        return this.graphHandle.peaks;
    }

    public Vector<Band> getBands() {
        Vector<Band> out = new Vector<Band>();

        Vector<Graph.Peak> peaks = this.computeGraph();

        for (int i = 0; i < peaks.size(); i++) {
            // vyseknut z povodneho! obrazka znacky, a ulozit do vektora. POZOR
            // !!!!!! Vysekavame z povodneho, takze
            // na suradnice vypocitane z imageCopy musime uplatnit inverznu
            // transformaciu
            Graph.Peak p = peaks.elementAt(i);
            out.add(new Band(this.image.getSubimage(0, (p.getLeft()), this.image.getWidth(), (p.getDiff()))));
        }
        return out;

    }

    public void verticalEdgeBi(BufferedImage image) {
        BufferedImage imageCopy = Photo.duplicateBufferedImage(image);
        float[] data = {-1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1};
        new ConvolveOp(new Kernel(3, 4, data), ConvolveOp.EDGE_NO_OP, null).filter(imageCopy, image);
    }

    // public void verticalRankBi(BufferedImage image) {
    // BufferedImage imageCopy = duplicateBi(image);
    //
    // float data[] = new float[9];
    // for (int i=0; i<data.length; i++) data[i] = 1.0f/data.length;
    //
    // new ConvolveOp(new Kernel(1,data.length, data), ConvolveOp.EDGE_NO_OP,
    // null).filter(imageCopy, image);
    // }

    public CarSnapshotGraph histogram(BufferedImage bi) {
        CarSnapshotGraph graph = new CarSnapshotGraph(this);
        for (int y = 0; y < bi.getHeight(); y++) {
            float counter = 0;
            for (int x = 0; x < bi.getWidth(); x++) {
                counter += Photo.getBrightness(bi, x, y);
            }
            graph.addPeak(counter);
        }
        return graph;
    }
}
