/**
 * 
 */
package org.lib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;

import org.junit.Test;

/**
 * @author oskopek
 * 
 */
public class LibraryTest {

    /*
     * TODO 3 Fix for some strange encodings of jpeg images - they don't always load correctly
     * See: http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file
     * B/W images load without a problem: for now - using snapshots/test_041.jpg
     */
    @Test
    public void intelligenceTest() throws Exception {
        final String image = "snapshots/test_041.jpg";

        /* Show raw loaded image for 5s
        InputStream is = Configurator.getConfigurator().getResourceAsStream(image);        
        BufferedImage bi = ImageIO.read(is);
        TestImageDraw t = new TestImageDraw(bi);
        Thread.sleep(5000);
        is = Configurator.getConfigurator().getResourceAsStream(image);
        */

        /* Show Photo loaded image for 5s
        Photo p = new Photo(is);
        t = new TestImageDraw(p.image);
        Thread.sleep(5000);
        p.close();
        */

        CarSnapshot carSnap = new CarSnapshot(image);
        assertNotNull("carSnap is null", carSnap);
        assertNotNull("carSnap.image is null", carSnap.image);

        /* Show CarSnapshot loaded image for 5s
        t = new TestImageDraw(carSnap.image);
        Thread.sleep(5000);
        t.frame.dispose();
        */

        Intelligence intel = new Intelligence();
        assertNotNull(intel);

        String spz = intel.recognize(carSnap);
        assertNotNull("The licence plate is null - are you sure the image has the correct color space?", spz);

        //System.out.println(spz);

        assertEquals("LM025BD", spz);

        //System.out.println(intel.lastProcessDuration());

    }

    public class TestImageDraw {

        public JFrame frame;
        BufferedImage img;
        public int WIDTH = 800;
        public int HEIGHT = 600;

        public TestImageDraw(BufferedImage img) {
            this.img = img;
            this.frame = new JFrame("WINDOW");
            this.frame.setVisible(true);

            this.start();
            this.frame.add(new JLabel(new ImageIcon(this.getImage())));

            this.frame.pack();
            //          frame.setSize(WIDTH, HEIGHT);
            // Better to DISPOSE than EXIT
            this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        public Image getImage() {
            return this.img;
        }

        public void start() {

            boolean running = true;
            while (running) {
                BufferStrategy bs = this.frame.getBufferStrategy();
                if (bs == null) {
                    this.frame.createBufferStrategy(4);
                    return;
                }

                Graphics g = bs.getDrawGraphics();
                g.drawImage(this.img, 0, 0, this.WIDTH, this.HEIGHT, null);
                g.dispose();
                bs.show();

            }
        }
    }

}
