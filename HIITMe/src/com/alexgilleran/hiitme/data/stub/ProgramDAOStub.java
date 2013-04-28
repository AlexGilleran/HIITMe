package com.alexgilleran.hiitme.data.stub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexgilleran.hiitme.data.ProgramDAO;
import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.alexgilleran.hiitme.model.Exercise.EffortLevel;
import com.alexgilleran.hiitme.model.impl.ProgramImpl;
import com.google.inject.Singleton;

@Singleton
public class ProgramDAOStub implements ProgramDAO {
	private List<Program> programList = new ArrayList<Program>();
	private Map<Long, Program> programMap = new HashMap<Long, Program>();

	public ProgramDAOStub() {
		ProgramImpl tabata = new ProgramImpl(1, "Tabata",
				"The tabata protocol", 8);
		tabata.addChildExercise("Hard", 2000, Exercise.EffortLevel.HARD, 1);
		tabata.addChildExercise("Rest", 1000, Exercise.EffortLevel.REST, 1);

		programMap.put(tabata.getId(), tabata);
		programList.add(tabata);

		ProgramImpl nestTest = new ProgramImpl(2, "NestTest",
				"A nested test program", 3);
		ProgramNode nestNode1 = nestTest.addChildNode(2);
		ProgramNode nestNode11 = nestNode1.addChildNode(2);
		ProgramNode nestNode111 = nestNode11.addChildNode(1);
		nestNode111.addChildExercise("Ex1", 1000, EffortLevel.HARD, 2);
		nestNode111.addChildExercise("Ex2", 2000, EffortLevel.HARD, 3);
		ProgramNode nestNode12 = nestNode1.addChildNode(3);
		nestNode12.addChildExercise("Ex3", 1500, EffortLevel.EASY, 1);
		ProgramNode nestNode2 = nestTest.addChildNode(1);
		nestNode2.addChildExercise("Ex4", 1000, EffortLevel.REST, 1);

		programMap.put(nestTest.getId(), nestTest);
		programList.add(nestTest);
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
