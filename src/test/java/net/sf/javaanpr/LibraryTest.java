/**
 * 
 */
package net.sf.javaanpr;

import net.sf.javaanpr.imageanalysis.CarSnapshot;
import net.sf.javaanpr.intelligence.Intelligence;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author oskopek
 *
 */
@Ignore
public class LibraryTest {
	
	 
    
    @Test
    public void intelligenceTest() throws Exception {
        Intelligence intel = new Intelligence();
        assertNotNull(intel);
        
        CarSnapshot carSnap = new CarSnapshot("snapshots/test_006.jpg");
        assertNotNull(carSnap);
        assertNotNull(carSnap.image);		
        
        String spz = intel.recognize(carSnap);
        assertNotNull("The licence plate is null", spz);
        
        System.out.println(spz);
        
        assertEquals("RK099AN", spz);
        assertTrue(("RK099AN".equals(spz)));
        
        //System.out.println(intel.lastProcessDuration());
    }

}
