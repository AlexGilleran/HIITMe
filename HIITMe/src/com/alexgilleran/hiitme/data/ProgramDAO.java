package com.alexgilleran.hiitme.data;

import java.util.List;

import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;

public interface ProgramDAO {
	List<Program> getAllPrograms();

	Program getProgram(long programId);

	void deleteAllPrograms();

	void replaceProgramNode(Program program, ProgramNode programNode);

}
