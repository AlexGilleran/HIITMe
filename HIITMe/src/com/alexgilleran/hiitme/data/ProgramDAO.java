package com.alexgilleran.hiitme.data;

import java.util.List;

import com.alexgilleran.hiitme.model.Program;

public interface ProgramDao {
	List<Program> getAllPrograms();

	Program getProgram(long programId);

	void deleteAllPrograms();
}
