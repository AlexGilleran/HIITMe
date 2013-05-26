package com.alexgilleran.hiitme.data;

import java.sql.SQLException;
import java.util.List;

import com.alexgilleran.hiitme.model.Program;

public interface ProgramDao {
	List<Program> getAllPrograms() throws SQLException;

	Program getProgram(long programId) throws SQLException;

}
