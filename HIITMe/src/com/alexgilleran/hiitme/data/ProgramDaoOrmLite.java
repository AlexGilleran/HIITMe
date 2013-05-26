package com.alexgilleran.hiitme.data;

import java.sql.SQLException;
import java.util.List;

import com.alexgilleran.hiitme.model.Program;
import com.j256.ormlite.dao.Dao;

public class ProgramDaoOrmLite implements ProgramDao {
	private Dao<Program, Long> programDao;

	public ProgramDaoOrmLite(HIITMeSqliteOpenHelper helper) {
		try {
			programDao = helper.getProgramDao();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Program> getAllPrograms() throws SQLException {
		return programDao.queryForAll();
	}

	@Override
	public Program getProgram(long programId) throws SQLException {
		return programDao.queryForId(programId);
	}
}
