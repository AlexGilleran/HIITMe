package com.alexgilleran.hiitme.data;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.alexgilleran.hiitme.model.Exercise;
import com.alexgilleran.hiitme.model.Program;
import com.alexgilleran.hiitme.model.ProgramNode;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class HIITMeSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
	private static String DB_NAME = "hiitme";
	private static int DB_VERSION = 1;

	public HIITMeSqliteOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Program.class);
			TableUtils.createTable(connectionSource, ProgramNode.class);
			TableUtils.createTable(connectionSource, Exercise.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, Program.class, true);
			TableUtils.dropTable(connectionSource, ProgramNode.class, true);
			TableUtils.dropTable(connectionSource, Exercise.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Dao<Program, Long> getProgramDao() throws SQLException {
		return getDao(Program.class);
	}

}
