package com.alexgilleran.hiitme.data;

import java.util.List;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;

public interface ProgramDao {
	List<Program> getAllPrograms();

	Program getProgram(long programId);

	ProgramNode getProgramNode(long programNodeId);

	ProgramNode getProgramNodeForProgram(long programId);

	List<ProgramNode> getChildrenOfNode(long programNodeId);

	Exercise getExerciseForNode(long programNodeId);

	void saveProgram(Program program);

	void saveProgramNode(ProgramNode node);

	void saveExercise(Exercise exercise);

}
