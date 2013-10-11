/**
 * 
 */
package net.sf.javaanpr;

import static org.junit.Assert.*;
import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;

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
        
        String spz = intel.recognize(new CarSnapshot("snapshots/test_006.jpg"));
        assertEquals("RK099AN", spz);
        //System.out.println(intel.lastProcessDuration());
    }

}
