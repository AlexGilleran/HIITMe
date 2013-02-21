package com.alexgilleran.hiitme.data.fake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Superset;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.alexgilleran.hiitme.model.impl.SuperSetImpl;
import com.alexgilleran.hiitme.model.impl.ExerciseImpl;
import com.google.inject.Singleton;

@Singleton
public class ProgramDAOFake implements ProgramDAO {
	private List<Program> programList = new ArrayList<Program>();
	private Map<Long, Program> programMap = new HashMap<Long, Program>();

	public ProgramDAOFake() {
		List<Exercise> repList = new ArrayList<Exercise>();
		Superset group = new SuperSetImpl(2, repList);

		repList.add(new ExerciseImpl("Hard", 2000, Exercise.EffortLevel.HARD,
				group));
		repList.add(new ExerciseImpl("Rest", 1000, Exercise.EffortLevel.REST,
				group));

		Program tabata = new ProgramImpl(1, "Tabata", "The tabata protocol",
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
