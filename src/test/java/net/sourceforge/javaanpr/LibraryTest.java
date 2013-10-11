/**
 * 
 */
package net.sourceforge.javaanpr;

import static org.junit.Assert.*;
import org.junit.Test;

import javaanpr.imageanalysis.CarSnapshot;
import javaanpr.intelligence.Intelligence;

/**
 * @author oskopek
 *
 */
public class LibraryTest {
    
    @Test
    public void intelligenceTest() throws Exception {
        Intelligence intel = new Intelligence(false);
        assertNotNull(intel);
        
        String spz = intel.recognize(new CarSnapshot("skoda_oct.jpg"));
        assertEquals("2SU358F", spz); //actually 2SU358F
        System.out.println(intel.lastProcessDuration());
    }

}
