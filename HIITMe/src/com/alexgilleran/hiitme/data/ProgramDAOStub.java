package com.alexgilleran.hiitme.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Exercise.EffortLevel;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.google.inject.Singleton;

@Singleton
public class ProgramDaoStub implements ProgramDao {
	private final List<Program> programList = new ArrayList<Program>();
	private final Map<Long, Program> programMap = new HashMap<Long, Program>();

	public ProgramDaoStub() {
		Program tabata = new Program("Tabata", "The tabata protocol", 8);
		tabata.getAssociatedNode().addChildExercise("Hard", 2000,
				Exercise.EffortLevel.HARD, 1);
		tabata.getAssociatedNode().addChildExercise("Rest", 1000,
				Exercise.EffortLevel.REST, 1);

		programMap.put(tabata.getId(), tabata);
		programList.add(tabata);

		Program nestTest = new Program("NestTest", "A nested test program", 3);
		ProgramNode nestNode1 = nestTest.getAssociatedNode().addChildNode(2);
		ProgramNode nestNode11 = nestNode1.addChildNode(2);
		ProgramNode nestNode111 = nestNode11.addChildNode(1);
		nestNode111.addChildExercise("Ex1", 1000, EffortLevel.HARD, 2);
		nestNode111.addChildExercise("Ex2", 2000, EffortLevel.HARD, 3);
		ProgramNode nestNode12 = nestNode1.addChildNode(3);
		nestNode12.addChildExercise("Ex3", 1500, EffortLevel.EASY, 1);
		ProgramNode nestNode2 = nestTest.getAssociatedNode().addChildNode(1);
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

	@Override
	public void deleteAllPrograms() {
		// TODO Auto-generated method stub
		
	}

}
