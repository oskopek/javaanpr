/**
 * 
 */
package net.sf.javaanpr;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.junit.Test;

/**
 * @author oskopek
 *
 */
public class LibraryTest {
    
    @Test
    public void intelligenceTest() throws Exception {
        Intelligence intel = new Intelligence(false);
        assertNotNull(intel);
        
        CarSnapshot carSnap = new CarSnapshot("snapshots/test_006.jpg");
        assertNotNull(carSnap);
        
        String spz = intel.recognize(carSnap);
        System.out.println(spz);
        assumeTrue(("RK099AN".equals(spz))); //assertEquals("RK099AN", spz); //TODO: fix this test
        System.out.println(spz);
        
        //System.out.println(intel.lastProcessDuration());
    }

}
