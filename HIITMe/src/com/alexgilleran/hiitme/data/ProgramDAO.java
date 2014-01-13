package com.alexgilleran.hiitme.data;

import java.util.List;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.Node;

public interface ProgramDAO {
	List<Program> getProgramList();

	Program getProgram(long programId);

	void replaceProgramNode(Program program, Node programNode);

	long insertProgram(Program program);
	
	void deleteProgram(Program program);
}
