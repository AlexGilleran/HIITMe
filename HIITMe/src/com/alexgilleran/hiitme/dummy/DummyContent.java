package com.alexgilleran.hiitme.dummy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Rep;
import com.alexgilleran.hiitme.model.RepGroup;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.alexgilleran.hiitme.model.impl.RepGroupImpl;
import com.alexgilleran.hiitme.model.impl.RepImpl;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Program> ITEMS = new ArrayList<Program>();

    static {
    	List<Rep> repList = new ArrayList<Rep>();
    	repList.add(new RepImpl("Hard", 20000, Rep.EffortLevel.HARD));
    	repList.add(new RepImpl("Rest", 10000, Rep.EffortLevel.REST));
    	
    	RepGroup group = new RepGroupImpl(8, repList);
    	
    	Program tabata = new ProgramImpl(0, "Tabata", "The tabata protocol", Arrays.asList(group)); 
    	
    	ITEMS.add(tabata);
    }

}
