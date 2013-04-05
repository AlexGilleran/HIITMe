package com.alexgilleran.hiitme.data.fake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.google.inject.Singleton;

@Singleton
public class ProgramDAOFake implements ProgramDAO {
	private List<Program> programList = new ArrayList<Program>();
	private Map<Long, Program> programMap = new HashMap<Long, Program>();

	public ProgramDAOFake() {
		ProgramImpl tabata = new ProgramImpl(1, "Tabata",
				"The tabata protocol", 1);

		tabata.addChildExercise("Hard", 2000, Exercise.EffortLevel.HARD, 1);
		tabata.addChildExercise("Rest", 1000, Exercise.EffortLevel.REST, 1);

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
