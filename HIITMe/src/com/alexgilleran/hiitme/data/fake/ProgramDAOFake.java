package com.alexgilleran.hiitme.data.fake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Rep;
import com.alexgilleran.hiitme.model.RepGroup;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.alexgilleran.hiitme.model.impl.RepGroupImpl;
import com.alexgilleran.hiitme.model.impl.RepImpl;
import com.google.inject.Singleton;

@Singleton
public class ProgramDAOFake implements ProgramDAO {
	private List<Program> programList = new ArrayList<Program>();
	private Map<Long, Program> programMap = new HashMap<Long, Program>();

	public ProgramDAOFake() {
		List<Rep> repList = new ArrayList<Rep>();
		repList.add(new RepImpl("Hard", 20000, Rep.EffortLevel.HARD));
		repList.add(new RepImpl("Rest", 10000, Rep.EffortLevel.REST));

		RepGroup group = new RepGroupImpl(8, repList);

		Program tabata = new ProgramImpl(0, "Tabata", "The tabata protocol",
				Arrays.asList(group));

		programMap.put(tabata.getId(), tabata);
		programList.add(tabata);
	}

	@Override
	public List<Program> getAllPrograms() {
		return Collections.unmodifiableList(programList);
	}

	@Override
	public Program getProgram(long id) {
		return programMap.get(id);
	}

}
