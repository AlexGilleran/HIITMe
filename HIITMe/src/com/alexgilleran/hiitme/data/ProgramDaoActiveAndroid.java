package com.alexgilleran.hiitme.data;

import java.util.List;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.alexgilleran.hiitme.model.Program;

public class ProgramDaoActiveAndroid implements ProgramDAO {

	@Override
	public List<Program> getAllPrograms() {
		return new Select().from(Program.class).execute();
	}

	@Override
	public Program getProgram(long programId) {
		return new Select().from(Program.class)
				.where("Id=?", Long.valueOf(programId)).executeSingle();
	}

	@Override
	public void deleteAllPrograms() {
		new Delete().from(Program.class).execute();
	}
}
